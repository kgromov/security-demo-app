package com.demo.app.filters;

import com.demo.app.services.AccessTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//@Component
@Slf4j
public class JwtAuthenticationFilter extends AuthenticationFilter {
    public JwtAuthenticationFilter(AuthenticationManager authenticationManager,
                                   AccessTokenService accessTokenService,
                                   UserDetailsService userDetailsService) {
        super(authenticationManager, request -> convert(accessTokenService, userDetailsService, request));
        super.setFailureHandler(this::onFailure);
        super.setSuccessHandler(this::onSuccess);
    }

    private static Authentication convert(AccessTokenService accessTokenService,
                                          UserDetailsService userDetailsService,
                                          HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        String jwt = null;
        if (StringUtils.hasLength(bearerToken) && bearerToken.startsWith("Bearer ")) {
            jwt = bearerToken.substring(7);
        }

        if (accessTokenService.isTokenValid(jwt)) {
            String username = accessTokenService.getUsernameFromJWT(jwt);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            return authentication;
        }
        throw new BadCredentialsException("Invalid JWT token provided");
    }

    private void onFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setCharacterEncoding("utf-8");
        response.getWriter().println("You are not Mr Robot... 🤖");
    }

    private void onSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        // noop
    }
}
