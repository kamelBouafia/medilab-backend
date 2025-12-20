package com.medilab.service;

import com.medilab.config.RabbitMQConfig;
import com.medilab.dto.NotificationRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationProducerService {

    private final RabbitTemplate rabbitTemplate;

    public void sendNotification(NotificationRequestDTO request) {
        log.info("Sending notification request to RabbitMQ: {}", request);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                request);
    }
}
