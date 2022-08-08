package com.demo.app;

import com.demo.app.config.JwtSettings;
import com.demo.app.config.SmsSettings;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@EnableWebSecurity
@EnableAsync
@EnableConfigurationProperties({JwtSettings.class, SmsSettings.class})
@SpringBootApplication
public class AuthWithMailApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthWithMailApplication.class, args);
    }
}
