package com.medilab.dto;

import lombok.Data;

@Data
public class StaffUserDto {
    private Long id;
    private String name;
    private String role;
    private Long labId;
}
