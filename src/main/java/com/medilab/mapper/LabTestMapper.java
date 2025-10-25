package com.medilab.mapper;

import com.medilab.dto.LabTestDto;
import com.medilab.entity.LabTest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LabTestMapper {

    @Mapping(target = "price", constant = "0.0") // Default value
    LabTestDto toDto(LabTest labTest);

    @Mapping(target = "lab", ignore = true)
    LabTest toEntity(LabTestDto dto);
}
