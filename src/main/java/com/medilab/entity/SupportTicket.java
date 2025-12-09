package com.medilab.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "support_tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_id", nullable = false, unique = true)
    private String ticketId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    private String subject;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    private Long labId;

    private Long userId;

    private String status;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;
}
