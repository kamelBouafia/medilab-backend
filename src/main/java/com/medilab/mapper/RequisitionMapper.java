package com.medilab.mapper;

import com.medilab.dto.LabTestDto;
import com.medilab.dto.RequisitionDto;
import com.medilab.entity.Requisition;
import com.medilab.entity.TestResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {DateTimeMapper.class, LabTestMapper.class})
public abstract class RequisitionMapper {

    @Mapping(source = "patient.id", target = "patientId")
    @Mapping(source = "patient.name", target = "patientName")
    @Mapping(source = "createdBy.id", target = "createdById")
    @Mapping(source = "requisition", target = "tests", qualifiedByName = "testsToTestsWithResults")
    public abstract RequisitionDto toDto(Requisition requisition);

    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lab", ignore = true)
    @Mapping(target = "tests", ignore = true)
    @Mapping(target = "completionDate", ignore = true)
    @Mapping(target = "date", ignore = true)
    public abstract Requisition toEntity(RequisitionDto dto);

    @Named("testsToTestsWithResults")
    public Set<LabTestDto> testsToTestsWithResults(Requisition requisition) {
        if (requisition.getTests() == null) {
            return Collections.emptySet();
        }

        List<TestResult> results = requisition.getTestResults();
        LabTestMapper labTestMapper = new LabTestMapperImpl();

        return requisition.getTests().stream().map(test -> {
            LabTestDto dto = labTestMapper.toDto(test);
            if (results != null) {
                results.stream()
                        .filter(result -> result.getTest().getId().equals(test.getId()))
                        .findFirst()
                        .ifPresent(result -> dto.setResult(result.getResultValue()));
            }
            return dto;
        }).collect(Collectors.toSet());
    }
}
