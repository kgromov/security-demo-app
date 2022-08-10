package com.demo.app.repositories;

import com.demo.app.model.OneTimePassword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OneTimePasswordRepository extends JpaRepository<OneTimePassword, Long> {
    Optional<OneTimePassword> findByUsername(String username);
}
