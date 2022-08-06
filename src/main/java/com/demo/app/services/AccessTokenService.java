package com.demo.app.services;

import com.demo.app.config.JwtSettings;
import com.demo.app.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.sql.Date;
import java.time.Instant;

import static io.jsonwebtoken.Jwts.parser;

@Service
@RequiredArgsConstructor
public class AccessTokenService {
    private final JwtSettings jwtSettings;
    private KeyStore keyStore;

    @PostConstruct
    @SneakyThrows
    public void init() {
        try (InputStream resourceAsStream = getClass().getResourceAsStream("/static/" + jwtSettings.getKeyStoreName())) {
            keyStore = KeyStore.getInstance("JKS");
            keyStore.load(resourceAsStream, jwtSettings.getKeyPassword().toCharArray());
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred while loading keystore");
        }
    }

    public String generateToken(Authentication authentication) {
        User principal = (User) authentication.getPrincipal();
        return generateTokenWithUserName(principal.getUsername());
    }

    public String generateToken(String username) {
        return generateTokenWithUserName(username);
    }

    private String generateTokenWithUserName(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plus(jwtSettings.getExpiredAfter())))
                .signWith(getPrivateKey())
                .compact();
    }

    private PrivateKey getPrivateKey() {
        try {
            return (PrivateKey) keyStore.getKey(jwtSettings.getKeyAlias(), jwtSettings.getKeyPassword().toCharArray());
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred while retrieving public key from keystore");
        }
    }

    private PublicKey getPublicKey() {
        try {
            return keyStore.getCertificate(jwtSettings.getKeyAlias()).getPublicKey();
        } catch (KeyStoreException e) {
            throw new RuntimeException("Exception occurred while retrieving public key from keystore");
        }
    }

    public boolean isTokenValid(String jwtToken) {
        if (!StringUtils.hasLength(jwtToken)) {
            return false;
        }
        PublicKey publicKey = getPublicKey();
        Jws<Claims> jws = parser().setSigningKey(publicKey).parseClaimsJws(jwtToken);
        return true;
    }

    public String getUsernameFromJWT(String token) {
        PublicKey publicKey = getPublicKey();
        Claims claims = parser()
                .setSigningKey(publicKey)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public static void main(String[] args) {
        String keyPassword = "s3cr3t";
        String keyAlias = "spring-security-demo";
        String keyPath = keyAlias + ".jks";
        try (InputStream resourceAsStream = AccessTokenService.class.getResourceAsStream("/static/" + keyPath)) {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(resourceAsStream, keyPassword.toCharArray());
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(keyAlias, keyPassword.toCharArray());
            PublicKey publicKey = keyStore.getCertificate(keyAlias).getPublicKey();
            int a =1;
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred while loading keystore");
        }
    }
}
