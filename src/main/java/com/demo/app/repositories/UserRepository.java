package com.demo.app.repositories;

import com.demo.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    List<User> findByEnabledFalse();

    void deleteByUsername(String username);

    boolean existsByUsername(String username);
}
