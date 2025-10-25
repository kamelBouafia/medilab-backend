package com.medilab.dto;

import lombok.Data;

import java.util.List;

@Data
public class RequisitionDto {
    private Long id;
    private Long patientId;
    private String doctorName;
    private String date;
    private List<Long> testIds;
    private String status;
    private Long createdById;
}
