package com.medilab.service;

import com.medilab.dto.SupportContactRequest;
import com.medilab.dto.SupportContactResponse;
import com.medilab.dto.SupportTicketDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SupportService {
    SupportContactResponse createSupportTicket(SupportContactRequest request);

    Page<SupportTicketDto> listSupportTickets(Pageable pageable);

    Page<SupportTicketDto> searchSupportTickets(String q, Long labId, String status, Long userId, Pageable pageable);

    SupportTicketDto getTicketById(Long id);

    SupportTicketDto updateTicket(Long id, SupportTicketDto ticketDto);

    void deleteTicket(Long id);
}
