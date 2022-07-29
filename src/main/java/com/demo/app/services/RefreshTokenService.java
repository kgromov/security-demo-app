package com.demo.app.services;

import com.demo.app.model.RefreshToken;
import com.demo.app.repositories.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshToken generateToken() {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setCreatedAt(Instant.now());

        return refreshTokenRepository.save(refreshToken);
    }

    public void validateToken(String token) {
        refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new EntityNotFoundException("Refresh token is invalid"));
    }

    public void deleteToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }
}
