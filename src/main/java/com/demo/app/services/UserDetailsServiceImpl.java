package com.demo.app.services;

import com.demo.app.model.User;
import com.demo.app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsManager {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
        // or try this one
        return org.springframework.security.core.userdetails.User.withUserDetails(user).build();
        /*return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getAuthorities())
                .disabled(!user.isEnabled())
                .accountExpired(!user.isAccountNonExpired())
                .accountLocked(!user.isAccountNonLocked())
                .credentialsExpired(user.isCredentialsNonExpired())
                .build();*/
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

    // TODO: invoke via endpoint
    @Override
    @Transactional
    public void changePassword(String oldPassword, String newPassword) {
        User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User loadedUser = userRepository.findByUsername(principal.getUsername()).orElseThrow();
        loadedUser.setPassword(newPassword);
        userRepository.save(loadedUser);
        // TODO: reauthenticate
    }

    @Override
    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }
}
