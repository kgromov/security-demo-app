package com.demo.app.services;

import com.demo.app.dtos.OneTimePasswordRequest;
import com.demo.app.model.User;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static java.util.concurrent.TimeUnit.MINUTES;

@Service
public class CachedOneTimePasswordService implements OneTimePasswordService {
    private final Cache<String, String> codesCache = Caffeine.newBuilder()
            .expireAfterWrite(5, MINUTES)
            .maximumSize(3)
            .build();

    @Override
    public void generateOTP(User user) {
        String otpCode = this.generateCode();
        codesCache.put(user.getUsername(), otpCode);
    }

    @Override
    public boolean isOtpValid(OneTimePasswordRequest otpRequest) {
        return Optional.ofNullable(codesCache.getIfPresent(otpRequest.getUsername()))
                .map(code -> code.equals(otpRequest.getCode()))
                .orElse(false);
    }
}
