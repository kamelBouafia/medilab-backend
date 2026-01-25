package com.medilab.dto;

import com.medilab.enums.PriceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgreementTestPriceDto {
    private Long id;

    @NotNull(message = "Test ID is required")
    private Long testId;

    private String testName;
    private String testCategory;

    @NotNull(message = "Inter-lab price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal interLabPrice;

    @NotNull(message = "Price type is required")
    private PriceType priceType;

    private BigDecimal discountPercentage;
    private BigDecimal patientPrice;
}
