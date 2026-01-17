package com.medilab.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffUserDto {
    private Long id;

    @NotBlank(message = "{validation.required}")
    private String name;

    @NotBlank(message = "{validation.required}")
    private String role;

    @NotNull(message = "{validation.required}")
    private Long labId;
    private String labName;
    private String username;
    private String email;
    private String phone;
    private boolean enabled;
    private boolean gdprAccepted;
}
