package com.demo.app.services;

import com.demo.app.filters.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenConfigurer extends AbstractHttpConfigurer<JwtTokenConfigurer, HttpSecurity> {
    private final AccessTokenService accessTokenService;
    private final UserDetailsService userDetailsService;

    @Override
    public void init(HttpSecurity http) throws Exception {
        http.setSharedObject(AccessTokenService.class, accessTokenService);
        http.setSharedObject(UserDetailsService.class, userDetailsService);
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        AccessTokenService accessTokenService = http.getSharedObject(AccessTokenService.class);
        UserDetailsService userDetailsService = http.getSharedObject(UserDetailsService.class);
        AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);
        JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(authenticationManager, accessTokenService, userDetailsService);
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
