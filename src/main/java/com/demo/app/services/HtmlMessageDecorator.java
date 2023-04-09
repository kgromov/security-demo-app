package com.demo.app.services;

import com.demo.app.dtos.SimpleMailMessage;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;

@Component
public class HtmlMessageDecorator {

    @SneakyThrows
    public SimpleMailMessage decorate(SimpleMailMessage message) {
        URL resource = HtmlMessageDecorator.class.getClassLoader().getResource("templates/email-simple-template.html");
        File templateFile = Paths.get(resource.toURI()).toFile();
        Document document = Jsoup.parse(templateFile);
        Element img = document.getElementsByTag("img").get(0);
        img.attr("src", message.getBase64Image());
        Element title = document.getElementsByTag("title").get(0);
        title.text(message.getSubject());
        String htmlBody = document.html();
        return SimpleMailMessage.builder()
                .from(message.getFrom())
                .to(message.getTo())
                .subject(message.getSubject())
                .body(htmlBody)
                .base64Image(message.getBase64Image())
                .build();
    }
}
