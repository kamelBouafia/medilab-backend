package com.medilab.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
    private Long patientId;
    private String patientName;
    private String doctorName;
    private String date;
    private Set<Long> testIds;
    private Set<LabTestDto> tests;
    private String status;
    private Long createdById;
    private String pdfObjectPath;
    private String pdfGeneratedAt;
}
