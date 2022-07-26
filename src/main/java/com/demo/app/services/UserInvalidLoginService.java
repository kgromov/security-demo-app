package com.demo.app.services;

import com.demo.app.model.User;
import com.demo.app.repositories.UserRepository;
import com.github.benmanes.caffeine.cache.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class UserInvalidLoginService {
    private static final int MAX_INVALID_ATTEMPTS = 3;
    static final Duration TIME_TO_BLOCK_LOGIN = Duration.ofMinutes(15);

    private final UnlockUserRemovalListener unlockUserRemovalListener;
    private final Cache<String, Boolean> lockedUsers;
    private final Map<String, AtomicInteger> invalidLoginAttempts = new ConcurrentHashMap<>();

    public UserInvalidLoginService(UnlockUserRemovalListener unlockUserRemovalListener) {
        this.unlockUserRemovalListener = unlockUserRemovalListener;
        this.lockedUsers = Caffeine.newBuilder()
                .expireAfterWrite(TIME_TO_BLOCK_LOGIN)
                .scheduler(Scheduler.systemScheduler())
                .removalListener(unlockUserRemovalListener)
                .build();
    }

    public void addInvalidAttempt(String username) {
        if (isUserLocked(username)) {
            return;
        }
        int invalidAttempts = invalidLoginAttempts.computeIfAbsent(username, attempts -> new AtomicInteger()).incrementAndGet();
        if (invalidAttempts >= MAX_INVALID_ATTEMPTS) {
            lockedUsers.put(username, true);
            invalidLoginAttempts.remove(username);
            unlockUserRemovalListener.lockUser(username);
            log.info("User's {} login is locked for {}", username, TIME_TO_BLOCK_LOGIN);
        }
    }

    public void onSuccessfulLogin(String username) {
        invalidLoginAttempts.remove(username);
    }

    public boolean isUserLocked(String username) {
        log.debug("Locked users: {}", lockedUsers.asMap().keySet());
        return lockedUsers.asMap().containsKey(username);
    }

    @Slf4j
    @Component
    @RequiredArgsConstructor
    public static class UnlockUserRemovalListener implements RemovalListener<String, Boolean> {
        private final UserRepository userRepository;

        @Override
        @Transactional
        public void onRemoval(@Nullable String username, @Nullable Boolean value, @NonNull RemovalCause cause) {
            log.info("Unlock {} to login. Reason {}", username, cause);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new EntityNotFoundException("User " + username + " not found"));
            user.setLocked(false);
            userRepository.saveAndFlush(user);
        }

        @Transactional
        public void lockUser(String username) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new EntityNotFoundException("User " + username + " not found"));
            user.setLocked(true);
            userRepository.save(user);
        }
    }
}
