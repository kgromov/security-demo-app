package com.demo.app.services;

import com.demo.app.config.JwtSettings;
import com.demo.app.dtos.*;
import com.demo.app.model.VerificationToken;
import com.demo.app.model.User;
import com.demo.app.repositories.VerificationTokenRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityNotFoundException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.lang.String.format;
import static java.time.Instant.now;
import static java.util.concurrent.TimeUnit.MINUTES;

// TODO: too many responsibilities - refactor
@Slf4j
@Service
@RequiredArgsConstructor
public class UserCredentialsService {
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailSenderService mailSenderService;
    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;
    private final UserDetailsManager userDetailsManager;
    private final Environment environment;
    private final JwtSettings jwtSettings;
    private final UsernamePasswordAuthenticationService authenticationService;
    private final OneTimePasswordService oneTimePasswordService;


    private final Cache<String, Authentication> authentications = Caffeine.newBuilder()
            .expireAfterWrite(5, MINUTES)
            .maximumSize(100)
            .build();

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
    public void signup(RegistrationRequest registerRequest) {
        User user = User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .email(registerRequest.getEmail())
                .phoneNumber(registerRequest.getPhoneNumber())
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
    public void login(LoginRequest loginRequest) {
        User user = (User) userDetailsManager.loadUserByUsername(loginRequest.getUsername());
        verifyLoginPassword(user, loginRequest);
        authenticationService.authenticate(loginRequest.getUsername(), loginRequest.getPassword());
        oneTimePasswordService.generateOTP(user);
    }

    // TODO: add logic with invalid attempts - either block or reauthenticate
    // Add logic for otp code expiration - seems one more endpoint required for this or login with credentials again
    public Optional<AuthenticationResponse> confirmLogin(OneTimePasswordRequest passwordRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.getName().equals(passwordRequest.getUsername())) {
            throw new BadCredentialsException("User is not authenticated");
        }
        if (!oneTimePasswordService.isOtpValid(passwordRequest)) {
            log.warn("Invalid OTP code");
            return Optional.empty();
        }
        String authenticationToken = accessTokenService.generateToken(authentication);
        return Optional.ofNullable(AuthenticationResponse.builder()
                .authenticationToken(authenticationToken)
                .refreshToken(refreshTokenService.generateToken().getToken())
                .expiredAt(Instant.now().plus(jwtSettings.getExpiredAfter()))
                .build());
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
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationTokenRepository.save(verificationToken);
        return token;
    }

    @Transactional
    public void verifyAccount(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new EntityNotFoundException("Invalid Token"));
        User user = (User) userDetailsManager.loadUserByUsername(verificationToken.getUser().getUsername());
        user.setEnabled(true);
        userDetailsManager.updateUser(user);
    }

    // Don't authenticate but send a link to login
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

    private void verifyLoginPassword(UserDetails user, LoginRequest loginRequest) {
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new AccessDeniedException("Login failed: password does not match");
        }
    }

    private void verifyUserFromRequest(User principal, ChangePasswordRequest changePasswordRequest) {
        if (principal == null || !principal.getUsername().equals(changePasswordRequest.getUsername())) {
            throw new AccessDeniedException(format("Invalid request to change password - " +
                    "user %s is not found in Security context", changePasswordRequest.getUsername()));
        }
    }
}
