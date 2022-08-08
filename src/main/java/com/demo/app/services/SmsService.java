package com.demo.app.services;

import com.demo.app.config.SmsSettings;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {
    private final SmsSettings smsSettings;

    @PostConstruct
    public void initTwilio() {
//        Twilio.init(System.getenv("TWILIO_ACCOUNT_SID"), System.getenv("TWILIO_AUTH_TOKEN"));
        Twilio.init(smsSettings.getSid(), smsSettings.getToken());
    }

    @Async
    public void sendCode(String toPhoneNumber, String code) {
        Message.creator(new PhoneNumber(toPhoneNumber),
                new PhoneNumber(smsSettings.getPhoneNumber()),
                code)
                .create();
        log.info("Verification code was sent to {} number", toPhoneNumber);
    }
}
