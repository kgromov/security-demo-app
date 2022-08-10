package com.demo.app.services;

import com.demo.app.config.SmsSettings;
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

// TODO: move to interface; make 2 implementations: current - db, another one with cache
@Slf4j
@Service
@RequiredArgsConstructor
public class OneTimePasswordService {
    private final OneTimePasswordRepository oneTimePasswordRepository;
    private final SmsService smsService;

    @Transactional
    public void generateOTP(User user) {
        String otpCode = this.generateCode();
        oneTimePasswordRepository.findByUsername(user.getUsername())
                .ifPresentOrElse(otp -> otp.setCode(otpCode), () -> {
                    OneTimePassword oneTimePassword = new OneTimePassword();
                    oneTimePassword.setUsername(user.getUsername());
                    oneTimePassword.setCode(otpCode);
                    oneTimePasswordRepository.save(oneTimePassword);
                });
        // send sms code for verification
        smsService.sendCode(user.getPhoneNumber(), otpCode);
    }

    @Transactional(readOnly = true)
    public boolean isOtpValid(OneTimePasswordRequest otpRequest) {
        OneTimePassword oneTimePassword = oneTimePasswordRepository.findByUsername(otpRequest.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("No otp for user = " + otpRequest.getUsername()));
        return oneTimePassword.getCode().equals(otpRequest.getCode());
    }

    @SneakyThrows
    private String generateCode() {
        SecureRandom random = SecureRandom.getInstanceStrong();
        int code = random.nextInt(9000) + 1000;
        return String.valueOf(code);
    }
}
