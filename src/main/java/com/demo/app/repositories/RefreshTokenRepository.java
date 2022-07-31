package com.demo.app.repositories;

import com.demo.app.model.RefreshToken;
import com.demo.app.model.User;
import com.demo.app.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    void deleteByToken(String token);
}
