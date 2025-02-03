package com.demo.app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@Data
@ConstructorBinding
@ConfigurationProperties("sendgrid.settings")
public class SendgridSettings {
    private String apiKey;
    private String sender;
    private String receiver;
}
