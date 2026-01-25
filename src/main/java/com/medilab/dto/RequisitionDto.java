package com.medilab.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequisitionDto {
    private Long id;

    @NotNull(message = "{validation.required}")
    private Long patientId;

    private String patientName;
    private String doctorName;
    private String date;

    @NotEmpty(message = "{validation.required}")
    private Set<Long> testIds;

    private Set<LabTestDto> tests;
    private String status;
    private Long createdById;
    private Long labId;
    private String labName;
    private String pdfObjectPath;
    private String pdfGeneratedAt;
    private boolean hasOutsourcedTests;
}
