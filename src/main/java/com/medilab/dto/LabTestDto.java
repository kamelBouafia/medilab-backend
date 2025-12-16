package com.medilab.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LabTestDto {
    private Long id;
    private String name;
    private String category;
    private java.math.BigDecimal price;
    private String result;
}
