package com.demo.app.controllers;

import com.demo.app.dtos.AuthenticationRequest;
import com.demo.app.dtos.AuthenticationResponse;
import com.demo.app.dtos.LoginRequest;
import com.demo.app.services.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.OK;

@RestController
@RequiredArgsConstructor
@RequestMapping("/authentication")
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/signup")
    public ResponseEntity signup(@RequestBody AuthenticationRequest authenticationRequest) {
        authenticationService.signup(authenticationRequest);
        return new ResponseEntity(HttpStatus.ACCEPTED);
    }

    @GetMapping("accountVerification/{token}")
    public ResponseEntity<String> verifyAccount(@PathVariable String token) {
        authenticationService.verifyAccount(token);
        return new ResponseEntity<>("Account Activated Successfully", OK);
    }

    @PostMapping("/signin")
    public ResponseEntity<AuthenticationResponse> signin(@RequestBody LoginRequest loginRequest) {
        AuthenticationResponse response = authenticationService.signin();
        return ResponseEntity.ok(response);
    }
}
