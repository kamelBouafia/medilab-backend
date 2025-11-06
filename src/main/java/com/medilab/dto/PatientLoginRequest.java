package com.medilab.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientLoginRequest {
    @NotBlank
    private String username;

    @NotNull
    private LocalDate dob;
}
