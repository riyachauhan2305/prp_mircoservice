package com.prp.authservice.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailSenderService {

    private final JavaMailSender mailSender;

    public EmailSenderService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String toEmail, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("your-email@example.com");  // Replace with your configured sender
        message.setTo(toEmail);
        message.setSubject("Your OTP Code");
        message.setText(body);

        mailSender.send(message);
    }
}
