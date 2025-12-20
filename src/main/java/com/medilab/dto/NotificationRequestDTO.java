package com.medilab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDTO implements Serializable {
    private String recipient;
    private String subject;
    private String content;
    private String type; // EMAIL, SMS
    private Map<String, Object> metadata;
}
