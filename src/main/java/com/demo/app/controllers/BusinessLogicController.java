package com.demo.app.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BusinessLogicController {

    @GetMapping("/greeting")
    public String greeting() {
        return "Hello world";
    }
}
