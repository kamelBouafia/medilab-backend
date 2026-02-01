package com.medilab.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.medilab.enums.TestCategory;
import com.medilab.enums.TestUnit;
import com.medilab.enums.TestType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LabTestDto {
    private Long id;

    @NotBlank(message = "{validation.required}")
    private String name;

    @NotNull(message = "{validation.required}")
    private TestCategory category;

    private TestType type;

    @NotNull(message = "{validation.required}")
    @PositiveOrZero(message = "{validation.positive}")
    private java.math.BigDecimal price;

    private TestUnit unit;
    private Double minVal;
    private Double maxVal;
    private Double criticalMinVal;
    private Double criticalMaxVal;
    private String result;
    private String labName;
    private String partnerLabName;
    private String description;
}
