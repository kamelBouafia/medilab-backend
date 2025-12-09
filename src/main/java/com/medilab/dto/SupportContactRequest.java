package com.medilab.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SupportContactRequest {
    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    private String subject;

    @NotBlank
    private String message;

    private Long labId;

    private Long userId;
}
