package com.medilab.dto;

import lombok.Data;
import com.medilab.enums.TestResultFlag;

@Data
public class TestResultDto {
    private Long id;
    private Long requisitionId;
    private Long testId;
    private String testName;
    private String resultValue;
    private String interpretation;
    private TestResultFlag flag;
    private Long enteredById;
}
