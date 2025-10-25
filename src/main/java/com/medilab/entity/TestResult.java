package com.medilab.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "test_results", uniqueConstraints = @UniqueConstraint(columnNames = {"requisitionId","testId"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TestResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String requisitionId;
    private String testId;
    private String resultValue;
    @Column(length = 4000)
    private String interpretation;
    private String enteredById;
    private String labId;
}
