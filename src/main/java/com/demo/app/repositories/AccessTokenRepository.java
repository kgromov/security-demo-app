package com.demo.app.repositories;

import com.demo.app.model.User;
import com.demo.app.model.AccessToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccessTokenRepository extends JpaRepository<AccessToken, Long> {
    Optional<AccessToken> findByToken(String token);

    Optional<AccessToken> findByUser(User user);

    Optional<AccessToken> findByUserId(Long userId);
}
