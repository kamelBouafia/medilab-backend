package com.medilab.dto;

import jakarta.validation.Valid;
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
public class ReviewAgreementDto {
    @NotNull(message = "Action is required")
    private ReviewAction action;

    @Valid
    private List<AgreementTestPriceDto> testPrices;

    private String notes;

    private LocalDateTime validFrom;
    private LocalDateTime validTo;

    public enum ReviewAction {
        APPROVE,
        COUNTER_OFFER,
        REJECT
    }
}
