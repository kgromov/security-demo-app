package com.demo.app.services;

import com.demo.app.config.JwtSettings;
import com.demo.app.dtos.*;
import com.demo.app.model.AccessToken;
import com.demo.app.model.User;
import com.demo.app.repositories.AccessTokenRepository;
import com.demo.app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityNotFoundException;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static java.lang.String.format;
import static java.time.Instant.now;

// TODO: too many responsibilities - refactor
@Slf4j
@Service
@RequiredArgsConstructor
public class UserCredentialsService {
    private final AccessTokenRepository accessTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailSenderService mailSenderService;
    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;
    private final UserDetailsManager userDetailsManager;
    private final Environment environment;
    private final JwtSettings jwtSettings;
    private final UsernamePasswordAuthenticationService authenticationService;

    @Value("${spring.application.name}")
    private String appName;

    @Value("${mail.verification.sender}")
    private String sender;

    @Value("${mail.verification.address}")
    private String address;

    @PostConstruct
    public void init() {
        Duration expiredAfter = jwtSettings.getExpiredAfter();
        log.debug("JWT expiration setting = {}", expiredAfter);
    }

    @Transactional
    public void signup(AuthenticationRequest registerRequest) {
        User user = User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .email(registerRequest.getEmail())
                .createdAt(now())
                .enabled(false)
                .authorities(Set.of("ROLE_USER"))
                .build();
        userDetailsManager.createUser(user);

        // send verification link to user email
        String token = generateAccessToken(user);
        SimpleMailMessage simpleMail = SimpleMailMessage.builder()
                .from(sender)
                .to(user.getEmail())
                .subject("Activate your account")
                .body(format("Thank you for signing up to %s, please click on the below url to activate your account: "
                        + "%s/authentication/accountVerification/%s", appName, address, token))
                .build();
        mailSenderService.sendMail(simpleMail);
    }

    @Transactional
    public AuthenticationResponse signin(LoginRequest loginRequest) {
        Authentication authentication = authenticationService.authenticate(loginRequest.getUsername(), loginRequest.getPassword());
        String authenticationToken = accessTokenService.generateToken(authentication);
        return AuthenticationResponse.builder()
                .authenticationToken(authenticationToken)
                .refreshToken(refreshTokenService.generateToken().getToken())
                .expiredAt(Instant.now().plus(jwtSettings.getExpiredAfter()))
                .build();
    }

    public AuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        refreshTokenService.validateToken(refreshTokenRequest.getToken());
        String token = accessTokenService.generateToken(refreshTokenRequest.getUsername());
        return AuthenticationResponse.builder()
                .authenticationToken(token)
                .refreshToken(refreshTokenRequest.getToken())
                .expiredAt(Instant.now().plusSeconds(jwtSettings.getExpiredAfter().getSeconds()))
                .build();
    }

    private String generateAccessToken(User user) {
        String token = UUID.randomUUID().toString();
        AccessToken accessToken = new AccessToken();
        accessToken.setToken(token);
        accessToken.setUser(user);
        accessTokenRepository.save(accessToken);
        return token;
    }

    @Transactional
    public void verifyAccount(String token) {
        AccessToken accessToken = accessTokenRepository.findByToken(token)
                .orElseThrow(() -> new EntityNotFoundException("Invalid Token"));
        User user = (User) userDetailsManager.loadUserByUsername(accessToken.getUser().getUsername());
        user.setEnabled(true);
        userDetailsManager.updateUser(user);
    }

    // the very naive implementation with open password in request
    public void changePassword(ChangePasswordRequest changePasswordRequest) {
        User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        verifyUserFromRequest(principal, changePasswordRequest);
        authenticationService.unauthenticate(principal.getUsername(), changePasswordRequest.getOldPassword());
        String encodedPassword = passwordEncoder.encode(changePasswordRequest.getNewPassword());
        userDetailsManager.changePassword(changePasswordRequest.getOldPassword(), encodedPassword);
        authenticationService.authenticate(principal.getUsername(), changePasswordRequest.getNewPassword());
        SimpleMailMessage simpleMail = SimpleMailMessage.builder()
                .from(sender)
                .to(principal.getEmail())
                .subject("Changing password")
                .body(format("Password was successfully changed for user %s", principal.getUsername()))
                .build();
        mailSenderService.sendMail(simpleMail);
    }

    private void verifyUserFromRequest(User principal, ChangePasswordRequest changePasswordRequest) {
        if (principal == null || !principal.getUsername().equals(changePasswordRequest.getUsername())) {
            throw new AccessDeniedException(format("Invalid request to change password - " +
                    "user %s is not found in Security context", changePasswordRequest.getUsername()));
        }
    }
}
