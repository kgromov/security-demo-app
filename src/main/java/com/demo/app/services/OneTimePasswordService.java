package com.demo.app.services;

import com.demo.app.dtos.OneTimePasswordRequest;
import com.demo.app.model.OneTimePassword;
import com.demo.app.model.User;
import com.demo.app.repositories.OneTimePasswordRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.security.SecureRandom;

// TODO: move to interface; make 2 implementations: current - db, sms - another one
@Slf4j
@Service
@RequiredArgsConstructor
public class OneTimePasswordService {
    private final OneTimePasswordRepository oneTimePasswordRepository;

    @Transactional
    public void generateOTP(User user) {
        String code = this.generateCode();
        oneTimePasswordRepository.findByUsername(user.getUsername())
                .ifPresentOrElse(otp -> otp.setCode(code), () -> {
                    OneTimePassword oneTimePassword = new OneTimePassword();
                    oneTimePassword.setUsername(user.getUsername());
                    oneTimePassword.setCode(code);
                    oneTimePasswordRepository.save(oneTimePassword);
                });

    }

    @Transactional(readOnly = true)
    public boolean isOtpValid(OneTimePasswordRequest otpRequest) {
        OneTimePassword oneTimePassword = oneTimePasswordRepository.findByUsername(otpRequest.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("No otp for user = " + otpRequest.getUsername()));
        return oneTimePassword.getCode().equals(otpRequest.getCode());
    }

    @SneakyThrows
    public String generateCode() {
        SecureRandom random = SecureRandom.getInstanceStrong();
        int code = random.nextInt(9000) + 1000;
        return String.valueOf(code);
    }
}
