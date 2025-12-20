package com.medilab.notification.provider;

import com.medilab.notification.dto.NotificationRequestDTO;

public interface NotificationProvider {
    void send(NotificationRequestDTO request);

    boolean supports(String type);
}
