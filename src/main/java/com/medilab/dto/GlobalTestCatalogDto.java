package com.medilab.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.medilab.enums.TestCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GlobalTestCatalogDto {
    private Long id;
    private String code;
    private TestCategory category;
    private String defaultUnit;
    private Map<String, String> names; // Language code -> Name
    private String description;
}
