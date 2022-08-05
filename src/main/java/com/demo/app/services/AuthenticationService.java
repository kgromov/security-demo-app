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
import java.util.UUID;

import static java.time.Instant.now;

// TODO: too many responsibilities - refactor
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final AccessTokenRepository accessTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailSenderService mailSenderService;
    private final AuthenticationManager authenticationManager;
    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;
    private final UserDetailsManager userDetailsManager;
    private final Environment environment;
    private final JwtSettings jwtSettings;

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
                .build();
        userRepository.save(user);

        // send verification link to user email
        String token = generateAccessToken(user);
        SimpleMailMessage simpleMail = SimpleMailMessage.builder()
                .from(sender)
                .to(user.getEmail())
                .subject("Activate your account")
                .body(String.format("Thank you for signing up to %s, please click on the below url to activate your account: "
                        + "%s/authentication/accountVerification/%s", appName, address, token))
                .build();
        mailSenderService.sendMail(simpleMail);
    }

    @Transactional
    public AuthenticationResponse signin(LoginRequest loginRequest) {
        // TODO: move logic regarding UsernamePasswordAuthenticationToken to another service.
        // Probably rename current service => LoginService/UserService?
        UsernamePasswordAuthenticationToken usernameToken = new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),
                loginRequest.getPassword());
        Authentication authenticate = authenticationManager.authenticate(usernameToken);
        SecurityContextHolder.getContext().setAuthentication(authenticate);
        String authenticationToken = accessTokenService.generateToken(authenticate);
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
        Long userId = accessToken.getUser().getId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User Not Found with id - " + userId));
        user.setEnabled(true);
        userRepository.save(user);
    }

    // the very naive implementation with open password in request
    public void changePassword(ChangePasswordRequest changePasswordRequest) {
        // TODO: verification
        User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userDetailsManager.changePassword(changePasswordRequest.getOldPassword(), changePasswordRequest.getNewPassword());
        SimpleMailMessage simpleMail = SimpleMailMessage.builder()
                .from(sender)
                .to(principal.getEmail())
                .subject("Changing password")
                .body(String.format("Password was successfully changed for user %s", principal.getUsername()))
                .build();
        mailSenderService.sendMail(simpleMail);
    }
}
