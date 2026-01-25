package com.medilab.mapper;

import com.medilab.dto.RequisitionDto;
import com.medilab.entity.Requisition;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = { DateTimeMapper.class })
public abstract class RequisitionMapper {

    @Mapping(source = "patient.id", target = "patientId")
    @Mapping(source = "patient.name", target = "patientName")
    @Mapping(source = "createdBy.id", target = "createdById")
    @Mapping(source = "lab.id", target = "labId")
    @Mapping(source = "lab.name", target = "labName")
    @Mapping(target = "tests", ignore = true)
    @Mapping(target = "testIds", ignore = true)
    @Mapping(target = "hasOutsourcedTests", ignore = true)
    public abstract RequisitionDto toDto(Requisition requisition);

    @org.mapstruct.AfterMapping
    protected void setHasOutsourcedTests(Requisition requisition, @org.mapstruct.MappingTarget RequisitionDto dto) {
        if (requisition.getTests() != null) {
            boolean hasOutsourced = requisition.getTests().stream()
                    .anyMatch(t -> t.getType() == com.medilab.enums.TestType.OUTSOURCED);
            dto.setHasOutsourcedTests(hasOutsourced);
        }
    }

    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lab", ignore = true)
    @Mapping(target = "tests", ignore = true)
    @Mapping(target = "completionDate", ignore = true)
    @Mapping(target = "date", ignore = true)
    @Mapping(target = "testResults", ignore = true)
    public abstract Requisition toEntity(RequisitionDto dto);
}
