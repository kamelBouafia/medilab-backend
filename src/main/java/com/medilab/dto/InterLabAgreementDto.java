package com.medilab.dto;

import com.medilab.enums.AgreementStatus;
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
public class InterLabAgreementDto {
    private Long id;
    private Long mainLabId;
    private String mainLabName;
    private Long partnerLabId;
    private String partnerLabName;
    private AgreementStatus status;
    private Long requestedById;
    private String requestedByName;
    private Long reviewedById;
    private String reviewedByName;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private Integer version;
    private String notes;
    private List<AgreementTestPriceDto> testPrices;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
