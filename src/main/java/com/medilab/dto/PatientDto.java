package com.medilab.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientDto {
    private Long id;

    @NotBlank(message = "{validation.required}")
    private String name;

    private String username;

    @NotBlank(message = "{validation.required}")
    private String phone;

    @Email(message = "{validation.invalid_email}")
    private String email;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "{validation.invalid_date}")
    private String dob; // YYYY-MM-DD

    @NotBlank(message = "{validation.required}")
    private String gender;

    private String address;
    private String bloodGroup;
    private String allergies;
    private Long createdById;
    private Long labId;
    private String labName;
    private boolean gdprAccepted;
}
