package com.demo.app.controllers;

import com.demo.app.dtos.*;
import com.demo.app.services.UserCredentialsService;
import com.demo.app.services.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/authentication")
public class AuthenticationController {
    private final UserCredentialsService userCredentialsService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@RequestBody AuthenticationRequest authenticationRequest) {
        userCredentialsService.signup(authenticationRequest);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @GetMapping("/accountVerification/{token}")
    public ResponseEntity<String> verifyAccount(@PathVariable String token) {
        userCredentialsService.verifyAccount(token);
        return ResponseEntity.ok("Account Activated Successfully");
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthenticationResponse> signin(@RequestBody LoginRequest loginRequest) {
        AuthenticationResponse response = userCredentialsService.signin(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refreshToken")
    public AuthenticationResponse refreshTokens(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        return userCredentialsService.refreshToken(refreshTokenRequest);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        refreshTokenService.deleteToken(refreshTokenRequest.getToken());
        return ResponseEntity.ok("Refresh Token Deleted Successfully");
    }

    @PostMapping("/changePassword")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        userCredentialsService.changePassword(changePasswordRequest);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}
