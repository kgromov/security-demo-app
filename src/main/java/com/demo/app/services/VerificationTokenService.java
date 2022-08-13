package com.demo.app.services;

import com.demo.app.model.User;
import com.demo.app.model.VerificationToken;
import com.demo.app.repositories.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerificationTokenService {
    private final VerificationTokenRepository verificationTokenRepository;

    @Transactional
    public String generateVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationTokenRepository.save(verificationToken);
        return token;
    }

    @Transactional(readOnly = true)
    public VerificationToken getVerificationToken(String token) {
        return verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new EntityNotFoundException("Invalid Token"));
    }
}
