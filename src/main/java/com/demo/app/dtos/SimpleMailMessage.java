package com.demo.app.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimpleMailMessage {
    private String id;
    private String from;
    private String to;
    private String subject;
    private String body;
    private String base64Image;
}
