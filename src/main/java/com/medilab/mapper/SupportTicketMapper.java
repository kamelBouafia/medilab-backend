package com.medilab.mapper;

import com.medilab.dto.SupportTicketDto;
import com.medilab.entity.SupportTicket;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface SupportTicketMapper {
    SupportTicketDto toDto(SupportTicket entity);

    SupportTicket toEntity(SupportTicketDto dto);
}

