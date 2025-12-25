package com.medilab.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.medilab.enums.TestResultFlag;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TestResultDto {
    private Long id;

    @NotNull(message = "{validation.required}")
    private Long requisitionId;

    @NotNull(message = "{validation.required}")
    private Long testId;

    private String testName;
    private String testCategory;
    private Double testPrice;
    private String testUnit;
    private Double testMinVal;
    private Double testMaxVal;
    private Double testCriticalMinVal;
    private Double testCriticalMaxVal;

    @NotBlank(message = "{validation.required}")
    private String resultValue;

    private String interpretation;
    private TestResultFlag flag;
    private Long enteredById;
}
