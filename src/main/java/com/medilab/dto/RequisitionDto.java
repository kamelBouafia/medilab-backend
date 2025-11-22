package com.medilab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequisitionDto {
    private Long id;
    private Long patientId;
    private String patientName;
    private String doctorName;
    private String date;
    private Set<Long> testIds;
    private String status;
    private Long createdById;
}
