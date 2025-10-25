package com.medilab.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "test_results", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"requisitionId", "testId"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class TestResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requisitionId", nullable = false)
    private Requisition requisition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "testId", nullable = false)
    private LabTest test;

    @Column(name = "resultValue", nullable = false)
    private String resultValue;

    @Lob
    private String interpretation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enteredById", nullable = false)
    private StaffUser enteredBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "labId", nullable = false)
    private Lab lab;
}
