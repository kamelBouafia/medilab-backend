package com.medilab.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.medilab.enums.TestCategory;
import com.medilab.enums.TestUnit;
import java.util.List;

@Entity
@Table(name = "lab_tests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class LabTest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @Column(nullable = false)
    private String code; // Unique per lab? or just internal code.

    @Enumerated(EnumType.STRING)
    private TestCategory category;

    @Enumerated(EnumType.STRING)
    private TestUnit unit;

    private Double minVal;
    private Double maxVal;
    private Double criticalMinVal;
    private Double criticalMaxVal;

    @Column(columnDefinition = "TEXT")
    private String description;

    private java.math.BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "global_test_id")
    private GlobalTestCatalog globalTest;

    @OneToMany(mappedBy = "labTest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestReferenceRange> referenceRanges;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_id", nullable = false)
    private Lab lab;
}
