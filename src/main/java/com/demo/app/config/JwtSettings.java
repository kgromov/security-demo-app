package com.demo.app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("jwt")
public class JwtSettings {
    private final String publicKey;
    private final String privateKey;
    private final String keyAlias;
    private final String keyStoreName;
}
