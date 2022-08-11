package com.demo.app.controllers;

import com.demo.app.dtos.*;
import com.demo.app.services.UserCredentialsService;
import com.demo.app.services.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

// TODO: Advice controller to map exceptions
@RestController
@RequiredArgsConstructor
@RequestMapping("/authentication")
public class AuthenticationController {
    private final UserCredentialsService userCredentialsService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@RequestBody RegistrationRequest registrationRequest) {
        userCredentialsService.signup(registrationRequest);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @GetMapping("/accountVerification/{verificationToken}")
    public ResponseEntity<String> activateAccount(@PathVariable String verificationToken) {
        userCredentialsService.verifyAccount(verificationToken);
        return ResponseEntity.ok("Account Activated Successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequest loginRequest) {
        userCredentialsService.login(loginRequest);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PostMapping("/confirmLogin")
    public ResponseEntity<AuthenticationResponse> confirmLogin(@RequestBody OneTimePasswordRequest oneTimePasswordRequest) {
        Optional<AuthenticationResponse> authenticationResponse = userCredentialsService.confirmLogin(oneTimePasswordRequest);
        return authenticationResponse.map(ResponseEntity::ok).orElseGet(() -> new ResponseEntity<>(HttpStatus.UNAUTHORIZED));
    }

    @PostMapping("/refreshToken")
    public AuthenticationResponse refreshTokens(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        return userCredentialsService.refreshToken(refreshTokenRequest);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        userCredentialsService.logout(refreshTokenRequest);
        return ResponseEntity.ok(refreshTokenRequest.getUsername() + " is logged out");
    }

    @PostMapping("/changePassword")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        userCredentialsService.changePassword(changePasswordRequest);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}
