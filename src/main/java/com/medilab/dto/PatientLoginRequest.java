package com.medilab.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientLoginRequest {
    private String username;
    private LocalDate dob;
}
