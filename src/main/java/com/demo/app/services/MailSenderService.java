package com.demo.app.services;

import com.demo.app.dtos.SimpleMailMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailSenderService {
    private final JavaMailSender mailSender;

    @Async
    public void sendMail(SimpleMailMessage mail) {
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            mimeMessage.setFrom(mail.getFrom());
            messageHelper.setTo(mail.getTo());
            messageHelper.setSubject(mail.getSubject());
            messageHelper.setText(mail.getBody());
        };
        mailSender.send(messagePreparator);
        log.info("Mail '{}' from {} to {} was successfully sent", mail.getSubject(), mail.getFrom(), mail.getTo());
    }
}
