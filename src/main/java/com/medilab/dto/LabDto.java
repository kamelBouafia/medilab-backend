package com.medilab.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LabDto {
    private Long id;
    private String name;
    private String location;
    private String contactEmail;
    private String licenseNumber;
    private LocalDateTime trialStart;
    private LocalDateTime trialEnd;
    private String defaultLanguage;
    private Long parentLabId;
}
