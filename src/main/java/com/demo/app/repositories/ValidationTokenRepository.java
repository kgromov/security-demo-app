package com.demo.app.repositories;

import com.demo.app.model.User;
import com.demo.app.model.ValidationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ValidationTokenRepository extends JpaRepository<ValidationToken, String> {
    Optional<ValidationToken> findByToken(String token);

    Optional<ValidationToken> findByUser(User user);

    Optional<ValidationToken> findByUserId(String userId);
}
