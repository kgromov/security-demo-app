package com.demo.app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Data
@Component
@ConfigurationProperties("jwt")
public class JwtSettings {
    private final String keyPassword;
    private final String keyAlias;
    private final String keyStoreName;
//    @DurationUnit(ChronoUnit.MINUTES)
    private final Duration expiredAfter;
}
