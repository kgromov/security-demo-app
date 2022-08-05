package com.demo.app.services;

import com.demo.app.model.User;
import com.demo.app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsManager {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        return org.springframework.security.core.userdetails.User.withUserDetails(user).build();
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
    public void changePassword(String oldPassword, String newPassword) {
//        User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String currentUserName = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getName)
                .orElseThrow(() -> new AccessDeniedException("Can't change password as no Authentication object found in context "));
        User loadedUser = userRepository.findByUsername(currentUserName).orElseThrow();
        // reauthenticate
        authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(currentUserName, oldPassword));
        loadedUser.setPassword(newPassword);
        userRepository.saveAndFlush(loadedUser);

        Authentication authentication = new UsernamePasswordAuthenticationToken(currentUserName, newPassword);
        Authentication authenticate = authenticationManager.authenticate(authentication);
        SecurityContextHolder.getContext().setAuthentication(authenticate);
    }

    @Override
    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }
}
