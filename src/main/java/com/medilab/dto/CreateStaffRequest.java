package com.medilab.dto;

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
public class CreateStaffRequest {
    @NotBlank(message = "{validation.required}")
    private String name;

    private String username;

    @NotBlank(message = "{validation.required}")
    @Pattern(regexp = "Manager|Technician|SYSTEM_ADMIN", message = "Invalid role")
    private String role;

    private Long labId;
    private String email;
    private String phone;
    private String tempPassword;
}
