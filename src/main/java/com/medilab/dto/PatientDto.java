package com.medilab.dto;

import lombok.Data;

@Data
public class PatientDto {
    private Long id;
    private String name;
    private String username;
    private String phone;
    private String email;
    private String dob; // YYYY-MM-DD
    private String gender;
    private String address;
    private String bloodGroup;
    private String allergies;
    private Long createdById;
    private Long labId;
}
