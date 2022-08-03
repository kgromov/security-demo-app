package com.demo.app.controllers;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;
import java.util.Random;

@RestController
public class BusinessLogicController {

    @GetMapping("/greeting")
    @Secured("ROLE_USER")
    public String greeting() {
        return "Hello world";
    }

    @GetMapping("/generateGreeting")
    @RolesAllowed("ADMIN")
    public String generateGreeting() {
        return "Hello " + new Random().nextGaussian();
    }

    @GetMapping("/greetingToUser")
    @PreAuthorize("hasAuthority('greete')")
    public String greetingToUser(Authentication authentication) {
        return "Hello, " +  authentication.getName();
    }


}
