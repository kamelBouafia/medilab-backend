package com.medilab.service.impl;

import com.medilab.dto.SupportContactRequest;
import com.medilab.dto.SupportContactResponse;
import com.medilab.dto.SupportTicketDto;
import com.medilab.entity.SupportTicket;
import com.medilab.mapper.SupportTicketMapper;
import com.medilab.repository.SupportTicketRepository;
import com.medilab.service.SupportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class SupportServiceImpl implements SupportService {

    private static final Logger log = LoggerFactory.getLogger(SupportServiceImpl.class);

    private final SupportTicketRepository repository;
    private final SupportTicketMapper mapper;

    @Autowired
    public SupportServiceImpl(SupportTicketRepository repository, SupportTicketMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public SupportContactResponse createSupportTicket(SupportContactRequest request) {
        String ticketId = "TKT-" + UUID.randomUUID();
        log.info("Creating support ticket {} for labId={} userId={}: {} - {}",
                ticketId, request.getLabId(), request.getUserId(), request.getSubject(), request.getMessage());

        SupportTicket ticket = SupportTicket.builder()
                .ticketId(ticketId)
                .name(request.getName())
                .email(request.getEmail())
                .subject(request.getSubject())
                .message(request.getMessage())
                .labId(request.getLabId())
                .userId(request.getUserId())
                .status("created")
                .createdAt(OffsetDateTime.now())
                .build();

        SupportTicket saved = repository.save(ticket);

        return new SupportContactResponse(saved.getTicketId(), saved.getStatus());
    }

    @Override
    public Page<SupportTicketDto> listSupportTickets(Pageable pageable) {
        Page<SupportTicket> page = repository.findAll(pageable);
        return page.map(mapper::toDto);
    }

    @Override
    public Page<SupportTicketDto> searchSupportTickets(String q, Long labId, String status, Pageable pageable) {
        Page<SupportTicket> page = repository.search((q == null || q.isBlank()) ? null : q, labId, (status == null || status.isBlank()) ? null : status, pageable);
        return page.map(mapper::toDto);
    }
}
