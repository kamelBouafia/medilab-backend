package com.medilab.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import com.medilab.enums.TestCategory;
import com.medilab.enums.TestUnit;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LabTestDto {
    private Long id;
    private String name;
    private TestCategory category;
    private java.math.BigDecimal price;
    private TestUnit unit;
    private Double minVal;
    private Double maxVal;
    private Double criticalMinVal;
    private Double criticalMaxVal;
    private String result;
}
