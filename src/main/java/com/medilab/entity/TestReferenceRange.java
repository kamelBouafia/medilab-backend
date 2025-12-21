package com.medilab.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "test_reference_ranges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestReferenceRange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_test_id", nullable = false)
    @JsonIgnore
    private LabTest labTest;

    private Integer minAge; // in years, null means no lower limit
    private Integer maxAge; // in years, null means no upper limit

    @Enumerated(EnumType.STRING)
    private Gender gender; // M, F, null for both

    // Normal Range
    private Double minVal;
    private Double maxVal;

    // Critical Range (Panic values)
    private Double criticalMin;
    private Double criticalMax;

    // Abnormal Range (Warning values)
    private Double abnormalMin;
    private Double abnormalMax;

    public enum Gender {
        M, F
    }
}
