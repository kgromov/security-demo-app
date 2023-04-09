package com.demo.app.services;

import com.demo.app.dtos.SimpleMailMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailSenderService {
    private final JavaMailSender mailSender;

    @Async
    public void sendMail(SimpleMailMessage mail) {
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessage.setFrom(mail.getFrom());
            messageHelper.setTo(mail.getTo());
            messageHelper.setSubject(mail.getSubject());
            messageHelper.setText(mail.getBody(), true);
            // attach image as file (just tried)
            /*if (Objects.nonNull(mail.getBase64Image())) {
                attachBase64Image(mail, messageHelper);
            }*/
        };
        mailSender.send(messagePreparator);
        log.info("Mail '{}' from {} to {} was successfully sent", mail.getSubject(), mail.getFrom(), mail.getTo());
    }

    private void attachBase64Image(SimpleMailMessage mail, MimeMessageHelper messageHelper) throws IOException, MessagingException {
        Path attachment = Files.createTempFile("attachment", ".png");
      byte[] bytes = Base64.getDecoder().decode(mail.getBase64Image().split(",")[1]);
//        byte[] bytes = org.apache.commons.codec.binary.Base64.decodeBase64(mail.getBase64Image().split(",")[1]);
        Files.write(attachment, bytes);
        messageHelper.addAttachment("qrCode", attachment.toFile());
    }
}
