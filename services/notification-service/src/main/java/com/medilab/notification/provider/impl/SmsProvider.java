package com.medilab.notification.provider.impl;

import com.medilab.notification.dto.NotificationRequestDTO;
import com.medilab.notification.provider.NotificationProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsProvider implements NotificationProvider {

    @Override
    public void send(NotificationRequestDTO request) {
        log.info("Sending SMS to {}: {}", request.getRecipient(), request.getContent());
        log.info("SMS logic placeholder - Integrate your SMPP/Modem logic here.");
    }

    @Override
    public boolean supports(String type) {
        return "SMS".equalsIgnoreCase(type);
    }
}
