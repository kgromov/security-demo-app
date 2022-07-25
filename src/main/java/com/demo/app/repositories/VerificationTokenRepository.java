package com.demo.app.repositories;

import com.demo.app.model.User;
import com.demo.app.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, String> {
    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findByUser(User user);

    Optional<VerificationToken> findByUserId(String userId);
}
