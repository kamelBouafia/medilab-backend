package com.medilab.mapper;

import com.medilab.dto.TestResultDto;
import com.medilab.entity.TestResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TestResultMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "requisition.id", target = "requisitionId")
    @Mapping(source = "test.id", target = "testId")
    @Mapping(source = "test.name", target = "testName")
    @Mapping(source = "resultValue", target = "resultValue")
    @Mapping(source = "interpretation", target = "interpretation")
    @Mapping(source = "flag", target = "flag")
    @Mapping(source = "enteredBy.id", target = "enteredById")
    TestResultDto toDto(TestResult testResult);
}
