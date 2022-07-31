package com.demo.app.services;

import com.demo.app.config.JwtSettings;
import com.demo.app.dtos.*;
import com.demo.app.model.User;
import com.demo.app.model.VerificationToken;
import com.demo.app.repositories.UserRepository;
import com.demo.app.repositories.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityNotFoundException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static java.time.Instant.now;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailSenderService mailSenderService;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;
    private final Environment environment;
    private final JwtSettings jwtSettings;

    @Value("${spring.application.name}")
    private String appName;

    @Value("${mail.verification.sender}")
    private String sender;

    @Value("${mail.verification.address}")
    private String address;

    @PostConstruct
    public void init(){
        Duration expiredAfter = jwtSettings.getExpiredAfter();
        log.info("JWT expiration setting = {}", expiredAfter);
    }


    @Transactional
    public void signup(AuthenticationRequest registerRequest) {
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setCreatedAt(now());
        user.setEnabled(false);

        userRepository.save(user);

        // send verification link to user email
        String token = generateVerificationToken(user);
        SimpleMailMessage simpleMail = SimpleMailMessage.builder()
                .from(sender)
                .to(user.getEmail())
                .subject("Activate your account")
                .body(String.format("Thank you for signing up to %s, please click on the below url to activate your account :"
                        + "%s/authentication/accountVerification/%s", appName, address, token))
                .build();
        mailSenderService.sendMail(simpleMail);
    }

    @Transactional
    public AuthenticationResponse signin(LoginRequest loginRequest) {
        UsernamePasswordAuthenticationToken usernameToken = new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),
                loginRequest.getPassword());
        Authentication authenticate = authenticationManager.authenticate(usernameToken);
        SecurityContextHolder.getContext().setAuthentication(authenticate);
        String authenticationToken = tokenService.generateToken(authenticate);
        return AuthenticationResponse.builder()
                .authenticationToken(authenticationToken)
                .refreshToken(refreshTokenService.generateToken().getToken())
                .expiredAt(Instant.now().plus(jwtSettings.getExpiredAfter()))
                .build();
    }

    public AuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        refreshTokenService.validateToken(refreshTokenRequest.getToken());
        String token = tokenService.generateToken(refreshTokenRequest.getUsername());
        return AuthenticationResponse.builder()
                .authenticationToken(token)
                .refreshToken(refreshTokenRequest.getToken())
                .expiredAt(Instant.now().plusSeconds(jwtSettings.getExpiredAfter().getSeconds()))
                .username(refreshTokenRequest.getUsername())
                .build();
    }

    private String generateVerificationToken(User user) {
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
        Long userId = verificationToken.getUser().getId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User Not Found with id - " + userId));
        user.setEnabled(true);
        userRepository.save(user);
    }
}
