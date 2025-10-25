package com.medilab.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "requisitions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Requisition {
    @Id
    private String id;
    private String patientId;
    private String doctorName;
    private OffsetDateTime date;
    private String status;
    private String createdById;
    private String labId;
}
