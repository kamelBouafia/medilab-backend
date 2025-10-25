package com.medilab.mapper;

import com.medilab.dto.PatientDto;
import com.medilab.entity.Patient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.time.LocalDate;
import java.time.Period;

@Mapper(componentModel = "spring", imports = {Period.class, LocalDate.class})
public interface PatientMapper {

    @Mappings({
        @Mapping(source = "createdBy.id", target = "createdById"),
        @Mapping(target = "age", expression = "java(Period.between(patient.getDob(), LocalDate.now()).getYears())")
    })
    PatientDto toDto(Patient patient);

    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lab", ignore = true)
    Patient toEntity(PatientDto dto);
}
