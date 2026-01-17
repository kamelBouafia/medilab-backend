package com.medilab.mapper;

import com.medilab.dto.StaffUserDto;
import com.medilab.entity.StaffUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StaffUserMapper {

    @Mapping(source = "lab.id", target = "labId")
    @Mapping(source = "lab.name", target = "labName")
    StaffUserDto toDto(StaffUser staffUser);

    List<StaffUserDto> toDtoList(List<StaffUser> staffUsers);

    // Added to explicitly address the error message if it's looking for this exact
    // method name
    List<StaffUserDto> toDTOs(List<StaffUser> staffUsers);
}
