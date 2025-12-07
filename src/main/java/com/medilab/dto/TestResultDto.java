package com.medilab.dto;

import lombok.Data;

@Data
public class TestResultDto {
    private Long id;
    private Long requisitionId;
    private Long testId;
    private String testName;
    private String resultValue;
    private String interpretation;
    private Long enteredById;
}
