package com.medilab.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "requisition_tests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RequisitionTest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String requisitionId;
    private String testId;
    private String labId;
}
