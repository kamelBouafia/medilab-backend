package com.medilab.dto;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class SupportTicketDto {
    private Long id;
    private String ticketId;
    private String name;
    private String email;
    private String subject;
    private String message;
    private Long labId;
    private Long userId;
    private String status;
    private OffsetDateTime createdAt;
}
