package com.demo.app.services;

import com.demo.app.model.User;
import com.demo.app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Primary
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsManager {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
//        return org.springframework.security.core.userdetails.User.withUserDetails(user).build();
        return user;
    }

    @Override
    @Transactional
    public void createUser(UserDetails user) {
        userRepository.save((User) user);
    }

    @Override
    @Transactional
    public void updateUser(UserDetails user) {
        userRepository.save((User) user);
    }

    @Override
    @Transactional
    public void deleteUser(String username) {
        userRepository.deleteByUsername(username);
    }

    @Override
    @Transactional
    public void changePassword(String oldPassword, String newEncodedPassword) {
        String currentUserName = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getName)
                .orElseThrow(() -> new AccessDeniedException("Can't change password as no Authentication object found in context "));
        User loadedUser = userRepository.findByUsername(currentUserName).orElseThrow();
        loadedUser.setPassword(newEncodedPassword);
        userRepository.saveAndFlush(loadedUser);
    }

    @Override
    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }
}
