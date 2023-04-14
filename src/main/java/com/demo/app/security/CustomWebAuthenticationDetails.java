package com.demo.app.security;

import lombok.*;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
public class CustomWebAuthenticationDetails extends WebAuthenticationDetails {
    private String token;
    public CustomWebAuthenticationDetails(HttpServletRequest request) {
        super(request);
        this.token = request.getParameter("token");
    }
}
