package com.medilab.notification.listener;

import com.medilab.notification.config.RabbitMQConfig;
import com.medilab.notification.dto.NotificationRequestDTO;
import com.medilab.notification.provider.NotificationProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {

    private final List<NotificationProvider> providers;

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleNotification(NotificationRequestDTO request) {
        log.info("Received notification request: {}", request);

        providers.stream()
                .filter(p -> p.supports(request.getType()))
                .findFirst()
                .ifPresentOrElse(
                        p -> p.send(request),
                        () -> log.warn("No provider found for notification type: {}", request.getType()));
    }
}
