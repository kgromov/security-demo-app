package com.demo.app;

import com.demo.app.dtos.SimpleMailMessage;
import com.demo.app.services.MailSenderService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AuthWithMailApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthWithMailApplication.class, args);
    }

    @Bean
    ApplicationRunner applicationRunner(MailSenderService mailSenderService) {
        return args -> {
            SimpleMailMessage simpleMail = SimpleMailMessage.builder()
                    .from("kostya.master@email.com")
                    .to("kostya.friend@email.com")
                    .subject("Greeting")
                    .body("Hello, my dear friend!")
                    .build();

            mailSenderService.sendMail(simpleMail);
        };
    }
}
