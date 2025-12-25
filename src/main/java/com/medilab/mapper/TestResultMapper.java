package com.medilab.mapper;

import com.medilab.dto.TestResultDto;
import com.medilab.entity.TestResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TestResultMapper {

    @Mapping(source = "requisition.id", target = "requisitionId")
    @Mapping(source = "test.id", target = "testId")
    @Mapping(source = "test.name", target = "testName")
    @Mapping(source = "test.category", target = "testCategory")
    @Mapping(source = "test.price", target = "testPrice")
    @Mapping(source = "test.unit", target = "testUnit")
    @Mapping(source = "test.minVal", target = "testMinVal")
    @Mapping(source = "test.maxVal", target = "testMaxVal")
    @Mapping(source = "test.criticalMinVal", target = "testCriticalMinVal")
    @Mapping(source = "test.criticalMaxVal", target = "testCriticalMaxVal")
    @Mapping(source = "enteredBy.id", target = "enteredById")
    TestResultDto toDto(TestResult testResult);

    @Mapping(target = "requisition", ignore = true)
    @Mapping(target = "test", ignore = true)
    @Mapping(target = "enteredBy", ignore = true)
    @Mapping(target = "lab", ignore = true)
    TestResult toEntity(TestResultDto dto);
}
