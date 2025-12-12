package com.medilab.dto;

import lombok.Data;

@Data
public class CreateStaffRequest {
    private String name;
    private String username;
    private String role; // Manager or Technician
    private String tempPassword; // optional; if present set and force change
}

