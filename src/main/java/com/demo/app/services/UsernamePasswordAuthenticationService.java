package com.demo.app.services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsernamePasswordAuthenticationService {
    private final AuthenticationManager authenticationManager;

    public Authentication authenticate(String username, String password) {
        UsernamePasswordAuthenticationToken usernameToken = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication = authenticationManager.authenticate(usernameToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authentication;
    }

    public void unauthenticate(String username, String password)  {
        authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(username, password));
    }
}
