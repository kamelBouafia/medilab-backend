package com.medilab.dto;

import lombok.Data;

@Data
public class LabRegistrationRequest {
    // Lab details
    private String labName;
    private String location;
    private String contactEmail;
    private String licenseNumber;

    // Admin details
    private String adminName;
    private String adminUsername;
    private String adminEmail;
    private String adminPassword;
}

