package com.medilab.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAgreementRequestDto {
    @NotNull(message = "Partner lab ID is required")
    private Long partnerLabId;

    @NotEmpty(message = "At least one test must be included")
    @Valid
    private List<AgreementTestPriceDto> testPrices;

    private String notes;

    private LocalDateTime validFrom;
    private LocalDateTime validTo;
}
