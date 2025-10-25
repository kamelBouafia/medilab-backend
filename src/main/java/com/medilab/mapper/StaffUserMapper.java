package com.medilab.mapper;

import com.medilab.dto.StaffUserDTO;
import com.medilab.entity.StaffUser;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StaffUserMapper {
    StaffUserDTO toDTO(StaffUser entity);

    List<StaffUserDTO> toDTOs(List<StaffUser> entities);
}
