package com.medilab.mapper;

import com.medilab.dto.PatientDto;
import com.medilab.entity.Patient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.Named;

import java.time.LocalDate;
import java.time.Period;

@Mapper(componentModel = "spring", uses = { LocalDateAdapter.class }, imports = { Period.class, LocalDate.class })
public interface PatientMapper {

    @Mappings({
            @Mapping(source = "createdBy.id", target = "createdById"),
            // @Mapping(target = "age", expression = "java(Period.between(patient.getDob(),
            // LocalDate.now()).getYears())"),
            @Mapping(source = "dob", target = "dob", qualifiedByName = "localDateToString"),
            @Mapping(source = "gender", target = "gender", qualifiedByName = "genderToString"),
            @Mapping(source = "lab.id", target = "labId"),
            @Mapping(source = "lab.name", target = "labName")
    })
    PatientDto toDto(Patient patient);

    @Mappings({
            @Mapping(target = "createdBy", ignore = true),
            @Mapping(target = "lab", ignore = true),
            @Mapping(source = "dob", target = "dob", qualifiedByName = "stringToLocalDate"),
            @Mapping(source = "gender", target = "gender", qualifiedByName = "stringToGender")
    })
    Patient toEntity(PatientDto dto);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "createdBy", ignore = true),
            @Mapping(target = "lab", ignore = true),
            @Mapping(source = "dob", target = "dob", qualifiedByName = "stringToLocalDate"),
            @Mapping(source = "gender", target = "gender", qualifiedByName = "stringToGender")
    })
    void updatePatientFromDto(PatientDto dto, @MappingTarget Patient patient);

    @Named("genderToString")
    default String genderToString(Patient.Gender gender) {
        return gender != null ? gender.name() : null;
    }

    @Named("stringToGender")
    default Patient.Gender stringToGender(String gender) {
        return gender != null ? Patient.Gender.valueOf(gender) : null;
    }
}
