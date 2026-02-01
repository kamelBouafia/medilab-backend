package com.medilab.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.medilab.enums.PriceType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "agreement_test_prices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class AgreementTestPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agreement_id", nullable = false)
    private InterLabAgreement agreement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lab_test_id", nullable = false)
    private LabTest labTest;

    @Column(name = "inter_lab_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal interLabPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "price_type", nullable = false)
    @Builder.Default
    private PriceType priceType = PriceType.FIXED;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @Column(name = "patient_price", precision = 10, scale = 2)
    private BigDecimal patientPrice;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
