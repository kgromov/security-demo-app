package com.demo.app.services;

import com.demo.app.config.JwtSettings;
import io.jsonwebtoken.Jwts;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.sql.Date;
import java.time.Instant;

@Service
public class TokenService {
    private KeyStore keyStore;
    @Autowired
    private JwtSettings jwtSettings;

    @PostConstruct
    @SneakyThrows
    public void init() {
        try (InputStream resourceAsStream = getClass().getResourceAsStream("/static/" + jwtSettings.getKeyStoreName())) {
            keyStore = KeyStore.getInstance("JKS");
            keyStore.load(resourceAsStream, jwtSettings.getPublicKey().toCharArray());
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred while loading keystore");
        }
    }

    public String generateToken(Authentication authentication) {
        User principal = (User) authentication.getPrincipal();
        return Jwts.builder()
                .setSubject(principal.getUsername())
                .setExpiration(Date.from(Instant.ofEpochSecond(15 * 60)))
                .signWith(getPrivateKey())
                .compact();
    }

    private PrivateKey getPrivateKey() {
        try {
            return (PrivateKey) keyStore.getKey(jwtSettings.getKeyAlias(), jwtSettings.getPublicKey().toCharArray());
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred while retrieving public key from keystore");
        }
    }
}
