package com.demo.app.services;

import com.demo.app.dtos.AuthenticationRequest;
import com.demo.app.dtos.AuthenticationResponse;
import com.demo.app.dtos.LoginRequest;
import com.demo.app.dtos.SimpleMailMessage;
import com.demo.app.model.User;
import com.demo.app.model.VerificationToken;
import com.demo.app.repositories.UserRepository;
import com.demo.app.repositories.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.UUID;

import static java.time.Instant.now;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailSenderService mailSenderService;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

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
        // TODO: replace with mail settings
        SimpleMailMessage simpleMail = SimpleMailMessage.builder()
                .from("kostya.master@email.com")
                .to(user.getEmail())
                .subject("Activate your account")
                .body("Thank you for signing up to {{app_name}}, please click on the below url to activate your account :"
                        + "{{app_url}}" + "/" + token)
                .build();
        mailSenderService.sendMail(simpleMail);
    }

    public AuthenticationResponse signin(LoginRequest loginRequest) {
        UsernamePasswordAuthenticationToken usernameToken = new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),
                loginRequest.getPassword());
        Authentication authenticate = authenticationManager.authenticate(usernameToken);
        SecurityContextHolder.getContext().setAuthentication(authenticate);
        String authenticationToken = tokenService.generateToken(authenticate);
        return new AuthenticationResponse(authenticationToken, loginRequest.getUsername(), null);
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
        String userId = verificationToken.getUser().getId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User Not Found with id - " + userId));
        user.setEnabled(true);
        userRepository.save(user);
    }
}
