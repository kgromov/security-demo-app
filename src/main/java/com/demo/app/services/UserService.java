package com.demo.app.services;

import com.demo.app.model.User;
import com.demo.app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional(readOnly = true)
    public List<User> getDisabledUsers() {
        return userRepository.findByEnabledFalse();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Transactional
    public void removeUsers(Collection<User> disabledUsers) {
        userRepository.deleteAllInBatch(disabledUsers);
    }
}
