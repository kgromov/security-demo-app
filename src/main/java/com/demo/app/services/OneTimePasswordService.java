package com.demo.app.services;

import com.demo.app.dtos.OneTimePasswordRequest;
import com.demo.app.model.User;
import lombok.SneakyThrows;

import java.security.SecureRandom;

public interface OneTimePasswordService {

    void generateOTP(User user);

    boolean isOtpValid(OneTimePasswordRequest otpRequest);

    @SneakyThrows
    default String generateCode() {
        SecureRandom random = SecureRandom.getInstanceStrong();
        int code = random.nextInt(9000) + 1000;
        return String.valueOf(code);
    }
}
