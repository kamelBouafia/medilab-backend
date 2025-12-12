package com.medilab.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "support_tickets")
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class SupportTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_id", nullable = false, unique = true)
    private String ticketId;

    @Column(nullable = false)
    private String name;

    @Column
    private String email;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String message;

    private Long labId;

    private Long userId;

    private String status;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    public SupportTicket(Long id, String ticketId, String name, String email, String subject, Long labId, Long userId, String status, OffsetDateTime createdAt) {
        this.id = id;
        this.ticketId = ticketId;
        this.name = name;
        this.email = email;
        this.subject = subject;
        this.labId = labId;
        this.userId = userId;
        this.status = status;
        this.createdAt = createdAt;
    }
}
