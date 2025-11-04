package com.medilab.mapper;

import com.medilab.dto.AuditLogDto;
import com.medilab.entity.AuditLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = DateTimeMapper.class)
public interface AuditLogMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.name", target = "userName")
    AuditLogDto toDto(AuditLog auditLog);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "lab", ignore = true)
    AuditLog toEntity(AuditLogDto dto);
}
