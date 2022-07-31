package com.demo.app.controllers;

import com.demo.app.dtos.AuthenticationRequest;
import com.demo.app.dtos.AuthenticationResponse;
import com.demo.app.dtos.LoginRequest;
import com.demo.app.dtos.RefreshTokenRequest;
import com.demo.app.services.AuthenticationService;
import com.demo.app.services.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/authentication")
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/signup")
    public ResponseEntity signup(@RequestBody AuthenticationRequest authenticationRequest) {
        authenticationService.signup(authenticationRequest);
        return new ResponseEntity(HttpStatus.ACCEPTED);
    }

    @GetMapping("/accountVerification/{token}")
    public ResponseEntity<String> verifyAccount(@PathVariable String token) {
        authenticationService.verifyAccount(token);
        return ResponseEntity.ok("Account Activated Successfully");
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthenticationResponse> signin(@RequestBody LoginRequest loginRequest) {
        AuthenticationResponse response = authenticationService.signin(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refreshToken")
    public AuthenticationResponse refreshTokens(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        return authenticationService.refreshToken(refreshTokenRequest);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        refreshTokenService.deleteToken(refreshTokenRequest.getToken());
        return ResponseEntity.ok("Refresh Token Deleted Successfully");
    }
}
