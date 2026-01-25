package com.medilab.mapper;

import com.medilab.dto.LabTestDto;
import com.medilab.entity.LabTest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface LabTestMapper {

    @Mapping(source = "lab.name", target = "labName")
    @Mapping(source = "partnerLab.name", target = "partnerLabName")
    @Mapping(target = "result", ignore = true)
    LabTestDto toDto(LabTest labTest);

    @Mapping(target = "lab", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "globalTest", ignore = true)
    @Mapping(target = "referenceRanges", ignore = true)
    @Mapping(target = "partnerLab", ignore = true)
    @Mapping(source = "description", target = "description")
    LabTest toEntity(LabTestDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lab", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "globalTest", ignore = true)
    @Mapping(target = "referenceRanges", ignore = true)
    @Mapping(target = "partnerLab", ignore = true)
    @Mapping(source = "description", target = "description")
    void updateEntityFromDto(LabTestDto dto, @MappingTarget LabTest labTest);
}
