package com.medilab.mapper;

import com.medilab.dto.TestResultDto;
import com.medilab.entity.TestResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TestResultMapper {

    @Mapping(source = "requisition.id", target = "requisitionId")
    @Mapping(source = "test.id", target = "testId")
    @Mapping(source = "enteredBy.id", target = "enteredById")
    TestResultDto toDto(TestResult testResult);

    @Mapping(target = "requisition", ignore = true)
    @Mapping(target = "test", ignore = true)
    @Mapping(target = "enteredBy", ignore = true)
    @Mapping(target = "lab", ignore = true)
    TestResult toEntity(TestResultDto dto);
}
