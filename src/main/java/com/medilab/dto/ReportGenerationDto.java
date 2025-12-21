package com.medilab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportGenerationDto {
    // Lab Information
    private String labName;
    private String labLocation;
    private String labContactEmail;
    private String labLicenseNumber;

    // Patient Information
    private String patientName;
    private LocalDate patientDob;
    private String patientGender;
    private String patientPhone;
    private String patientEmail;
    private String patientAddress;
    private String patientBloodGroup;
    private String patientAllergies;

    // Requisition Information
    private Long requisitionId;
    private String doctorName;
    private LocalDateTime requisitionDate;
    private LocalDateTime completionDate;

    // Test Results
    private List<TestResultDetail> testResults;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestResultDetail {
        private String testName;
        private String testCategory;
        private String resultValue;
        private String referenceRange;
        private String interpretation;
        private String flag;
    }
}
