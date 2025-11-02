package com.medilab.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientLoginRequest {
    private Long labId;
    private Long patientId;
    private LocalDate dob;
}
