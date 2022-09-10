package com.demo.app.services;

import com.demo.app.config.JwtSettings;
import com.demo.app.dtos.*;
import com.demo.app.model.User;
import com.demo.app.model.VerificationToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static com.demo.app.services.UserInvalidLoginService.TIME_TO_BLOCK_LOGIN;
import static java.lang.String.format;
import static java.time.Instant.now;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCredentialsService {
    private final VerificationTokenService verificationTokenService;
    private final PasswordEncoder passwordEncoder;
    private final MailSenderService mailSenderService;
    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;
    private final UserDetailsManager userDetailsManager;
    private final JwtSettings jwtSettings;
    private final UsernamePasswordAuthenticationService authenticationService;
    private final OneTimePasswordService oneTimePasswordService;
    private final UserInvalidLoginService userInvalidLoginService;

    @Value("${spring.application.name}")
    private String appName;

    @Value("${mail.verification.sender}")
    private String sender;

    @Value("${mail.verification.address}")
    private String address;

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
        String token = verificationTokenService.generateVerificationToken(user);
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
    public void verifyAccount(String token) {
        VerificationToken verificationToken = verificationTokenService.getVerificationToken(token);
        User user = (User) userDetailsManager.loadUserByUsername(verificationToken.getUser().getUsername());
        user.setEnabled(true);
        userDetailsManager.updateUser(user);
    }

    public void login(LoginRequest loginRequest) {
        User user = (User) userDetailsManager.loadUserByUsername(loginRequest.getUsername());
        verifyLoginPassword(user, loginRequest);
        userInvalidLoginService.onSuccessfulLogin(user.getUsername());
        authenticationService.authenticate(loginRequest.getUsername(), loginRequest.getPassword());
        oneTimePasswordService.generateOTP(user);
    }

    public Optional<AuthenticationResponse> confirmLogin(OneTimePasswordRequest passwordRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.getName().equals(passwordRequest.getUsername())) {
            throw new BadCredentialsException("User is not authenticated");
        }
        if (!oneTimePasswordService.isOtpValid(passwordRequest)) {
            log.warn("Invalid OTP code");
            User user = (User) userDetailsManager.loadUserByUsername(passwordRequest.getUsername());
            userInvalidLoginService.addInvalidAttempt(passwordRequest.getUsername());
            if (!user.isLocked()) {
                oneTimePasswordService.generateOTP(user);
            }
            return Optional.empty();
        }
        userInvalidLoginService.onSuccessfulLogin(passwordRequest.getUsername());
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

    @Transactional
    public void logout(RefreshTokenRequest refreshTokenRequest) {
        refreshTokenService.deleteToken(refreshTokenRequest.getToken());
        log.debug("Refresh Token Deleted Successfully");
        authenticationService.unauthenticate(refreshTokenRequest.getUsername(), null);
        log.debug("User " + refreshTokenRequest.getUsername() + " is logged out");
    }

    // the very naive implementation with open password in request
    public void changePassword(ChangePasswordRequest changePasswordRequest) {
        User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        verifyUserFromRequest(principal, changePasswordRequest);
        authenticationService.unauthenticate(principal.getUsername(), changePasswordRequest.getOldPassword());
        String encodedPassword = passwordEncoder.encode(changePasswordRequest.getNewPassword());
        userDetailsManager.changePassword(changePasswordRequest.getOldPassword(), encodedPassword);
        String mailBody = new StringBuilder()
                .append("Password was successfully changed for user")
                .append(principal.getUsername()).append('\n')
                .append("Navigate to ").append(address).append("/authentication/login")
                .append("  login with new credentials")
                .toString();
        SimpleMailMessage simpleMail = SimpleMailMessage.builder()
                .from(sender)
                .to(principal.getEmail())
                .subject("Changing password")
                .body(mailBody)
                .build();
        mailSenderService.sendMail(simpleMail);
    }

    @Transactional
    public void generateNewOtpCode(String username) {
        User user = (User) userDetailsManager.loadUserByUsername(username);
        if (!(user.isEnabled() && user.isAccountNonLocked())) {
            throw new AccessDeniedException("In order to generate otp code user should be enabled and not locked");
        }
        oneTimePasswordService.generateOTP(user);
    }

    private void verifyLoginPassword(User user, LoginRequest loginRequest) {
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            userInvalidLoginService.addInvalidAttempt(user.getUsername());
            if (user.isLocked()) {
                throw new AccessDeniedException("Login failed: user is locked for " + TIME_TO_BLOCK_LOGIN);
            }
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
