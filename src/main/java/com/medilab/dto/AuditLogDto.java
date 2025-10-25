package com.medilab.dto;

import lombok.Data;

@Data
public class AuditLogDto {
    private Long id;
    private String timestamp;
    private Long userId;
    private String action;
    private String details;
}
