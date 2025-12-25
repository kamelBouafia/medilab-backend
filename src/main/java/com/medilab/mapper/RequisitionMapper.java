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
    public abstract RequisitionDto toDto(Requisition requisition);

    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lab", ignore = true)
    @Mapping(target = "tests", ignore = true)
    @Mapping(target = "completionDate", ignore = true)
    @Mapping(target = "date", ignore = true)
    public abstract Requisition toEntity(RequisitionDto dto);
}
