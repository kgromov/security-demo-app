package com.demo.app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@Data
@ConstructorBinding
@ConfigurationProperties("sms.settings")
public class SmsSettings {
    private String sid;
    private String token;
    private String phoneNumber;
}
