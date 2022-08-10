package com.demo.app.services;

import com.demo.app.config.JwtSettings;
import com.demo.app.dtos.AuthenticationResponse;
import com.demo.app.dtos.RefreshTokenRequest;
import com.demo.app.model.VerificationToken;
import com.demo.app.model.User;
import com.demo.app.repositories.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.UUID;

// TODO: refactoring is required:
// Move dao layer and all stuff related to AccessToken to AccessTokenService
// In AccessTokenService => JwtTokenService - all Jwt related stuff
// Encapsulate logic regardind token in this service and invoke in AuthenticationService
//@Service
@RequiredArgsConstructor
public class TokensManager {
    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final JwtSettings jwtSettings;

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
}
