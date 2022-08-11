package com.demo.app.services;

import com.demo.app.dtos.OneTimePasswordRequest;
import com.demo.app.model.OneTimePassword;
import com.demo.app.model.User;
import com.demo.app.repositories.OneTimePasswordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

@Slf4j
@Primary
@Service
@RequiredArgsConstructor
public class DbOneTimePasswordService implements OneTimePasswordService {
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


}
