package com.medilab.mapper;

import com.medilab.dto.SupportTicketDto;
import com.medilab.entity.SupportTicket;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SupportTicketMapper {
    SupportTicketDto toDto(SupportTicket entity);

    SupportTicket toEntity(SupportTicketDto dto);
}

