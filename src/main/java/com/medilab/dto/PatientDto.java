package com.medilab.dto;

import lombok.Data;

@Data
public class PatientDto {
    private Long id;
    private String name;
    private int age;
    private String gender;
    private String contact;
    private String dob; // YYYY-MM-DD
    private Long createdById;
}
