package com.medilab.dto;

import lombok.Data;

@Data
public class LabTestDto {
    private Long id;
    private String name;
    private String category;
    private double price;
}
