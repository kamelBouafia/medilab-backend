package com.medilab.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "requisitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Requisition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "doctorName")
    private String doctorName;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private OffsetDateTime date;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SampleStatus status = SampleStatus.PROCESSING;

    @Column(name = "completion_date")
    private LocalDateTime completionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private StaffUser createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_id", nullable = false)
    private Lab lab;

    @ManyToMany
    @JoinTable(name = "requisition_tests", joinColumns = @JoinColumn(name = "requisition_id"), inverseJoinColumns = @JoinColumn(name = "test_id"))
    private Set<LabTest> tests;

    @OneToMany(mappedBy = "requisition")
    private List<TestResult> testResults;

    @Column(name = "pdf_object_path", length = 500)
    private String pdfObjectPath;

    @Column(name = "pdf_generated_at")
    private LocalDateTime pdfGeneratedAt;
}
