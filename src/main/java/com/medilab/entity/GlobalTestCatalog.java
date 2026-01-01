package com.medilab.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Map;

@Entity
@Table(name = "global_test_catalog")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GlobalTestCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code; // e.g. CBC, GLU

    @Enumerated(EnumType.STRING)
    private com.medilab.enums.TestCategory category;

    private String defaultUnit;

    private Double defaultMinVal;
    private Double defaultMaxVal;
    private Double defaultCriticalMin;
    private Double defaultCriticalMax;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "global_test_names", joinColumns = @JoinColumn(name = "test_id"))
    @MapKeyColumn(name = "language_code") // e.g. "en", "es", "fr"
    @Column(name = "name")
    private Map<String, String> names;

    @Column(columnDefinition = "TEXT")
    private String description;
}
