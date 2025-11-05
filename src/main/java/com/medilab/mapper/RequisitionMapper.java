package com.medilab.mapper;

import com.medilab.dto.RequisitionDto;
import com.medilab.entity.LabTest;
import com.medilab.entity.Requisition;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = DateTimeMapper.class)
public interface RequisitionMapper {

    @Mapping(source = "patient.id", target = "patientId")
    @Mapping(source = "patient.name", target = "patientName")
    @Mapping(source = "createdBy.id", target = "createdById")
    @Mapping(source = "tests", target = "testIds", qualifiedByName = "testsToTestIds")
    RequisitionDto toDto(Requisition requisition);

    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lab", ignore = true)
    @Mapping(target = "tests", ignore = true)
    Requisition toEntity(RequisitionDto dto);

    @Named("testsToTestIds")
    default List<Long> testsToTestIds(Set<LabTest> tests) {
        if (tests == null) {
            return java.util.Collections.emptyList();
        }
        return tests.stream().map(LabTest::getId).collect(Collectors.toList());
    }
}
