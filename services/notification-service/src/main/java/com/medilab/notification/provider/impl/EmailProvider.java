package com.medilab.notification.provider.impl;

import com.medilab.notification.dto.NotificationRequestDTO;
import com.medilab.notification.provider.NotificationProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailProvider implements NotificationProvider {

    private final JavaMailSender mailSender;

    @Override
    public void send(NotificationRequestDTO request) {
        log.info("Sending Email to {}", request.getRecipient());
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(request.getRecipient());
            message.setSubject(request.getSubject());
            message.setText(request.getContent());
            mailSender.send(message);
            log.info("Email sent successfully to {}", request.getRecipient());
        } catch (Exception e) {
            log.error("Failed to send email to {}", request.getRecipient(), e);
            // In a real app, you might rethrow to trigger RabbitMQ retry
        }
    }

    @Override
    public boolean supports(String type) {
        return "EMAIL".equalsIgnoreCase(type);
    }
}
