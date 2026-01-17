package com.medilab.mapper;

import com.medilab.dto.LabTestDto;
import com.medilab.entity.LabTest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface LabTestMapper {

    @Mapping(source = "lab.name", target = "labName")
    LabTestDto toDto(LabTest labTest);

    @Mapping(target = "lab", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "globalTest", ignore = true)
    @Mapping(target = "referenceRanges", ignore = true)
    LabTest toEntity(LabTestDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lab", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "globalTest", ignore = true)
    @Mapping(target = "referenceRanges", ignore = true)
    void updateEntityFromDto(LabTestDto dto, @MappingTarget LabTest labTest);
}
