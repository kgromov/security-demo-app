package com.demo.app.services;

import com.demo.app.model.User;
import com.demo.app.repositories.UserRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class UserInvalidLoginService {
    private static final int MAX_INVALID_ATTEMPTS = 3;
    private final UnlockUserRemovalListener unlockUserRemovalListener;
    private final Cache<String, Boolean> lockedUsers;
    private final Map<String, AtomicInteger> invalidLoginAttempts = new ConcurrentHashMap<>();

    public UserInvalidLoginService(UnlockUserRemovalListener unlockUserRemovalListener) {
        this.unlockUserRemovalListener = unlockUserRemovalListener;
        this.lockedUsers = Caffeine.newBuilder()
//            .expireAfterWrite(30, TimeUnit.MINUTES)
                .expireAfterWrite(30, TimeUnit.SECONDS)
                .evictionListener(unlockUserRemovalListener)
                .build();
    }

    public void addInvalidAttempt(String username) {
        int invalidAttempts = invalidLoginAttempts.computeIfAbsent(username, attempts -> new AtomicInteger()).incrementAndGet();
        if (invalidAttempts >= MAX_INVALID_ATTEMPTS) {
            lockedUsers.put(username, true);
            invalidLoginAttempts.remove(username);
        }
    }

    public void onSuccessfulLogin(String username) {
        invalidLoginAttempts.remove(username);
    }

    @Component
    @RequiredArgsConstructor
    public static class UnlockUserRemovalListener implements RemovalListener<String, Boolean> {
        private final UserRepository userRepository;

        @Override
        @Transactional
        public void onRemoval(@Nullable String username, @Nullable Boolean value, @NonNull RemovalCause cause) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new EntityNotFoundException("User " + username + " not found"));
            user.setLocked(false);
            userRepository.saveAndFlush(user);
        }
    }
}
