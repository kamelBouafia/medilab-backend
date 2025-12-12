package com.medilab.service.impl;

import com.medilab.dto.SupportContactRequest;
import com.medilab.dto.SupportContactResponse;
import com.medilab.dto.SupportTicketDto;
import com.medilab.entity.StaffUser;
import com.medilab.entity.SupportTicket;
import com.medilab.exception.ResourceNotFoundException;
import com.medilab.mapper.SupportTicketMapper;
import com.medilab.repository.SupportTicketRepository;
import com.medilab.security.AuthenticatedUser;
import com.medilab.service.SupportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public Page<SupportTicketDto> searchSupportTickets(String q, Long labId, String status, Long userId, Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        boolean isSysAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("Admin"::equals);

        String q1 = (q == null || q.isBlank()) ? null : q;
        String status1 = (status == null || status.isBlank()) ? null : status;
        if (isSysAdmin) {
            // System admins can see all tickets, so we don't enforce the userId
            Page<SupportTicket> page = repository.search(q1, labId, status1, null, pageable);
            return page.map(mapper::toDto);
        } else {
            // Other users can only see their own tickets
            Object principal = authentication.getPrincipal();
            Long currentUserId = ((AuthenticatedUser) principal).getId();
            Page<SupportTicket> page = repository.search(q1, labId, status1, currentUserId, pageable);
            return page.map(mapper::toDto);
        }
    }

    @Override
    public SupportTicketDto getTicketById(Long id) {
        SupportTicket ticket = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Support ticket not found with id: " + id));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        Long currentUserId = ((AuthenticatedUser) principal).getId();

        boolean isSysAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("Admin"::equals);

        if (!isSysAdmin && !currentUserId.equals(ticket.getUserId())) {
            throw new AccessDeniedException("You are not authorized to view this ticket.");
        }

        return mapper.toDto(ticket);
    }

    @Override
    public SupportTicketDto updateTicket(Long id, SupportTicketDto ticketDto) {
        SupportTicket ticket = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Support ticket not found with id: " + id));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        Long currentUserId = ((AuthenticatedUser) principal).getId();

        boolean isSysAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("Admin"::equals);

        if (!isSysAdmin && !currentUserId.equals(ticket.getUserId())) {
            throw new AccessDeniedException("You are not authorized to update this ticket.");
        }

        ticket.setSubject(ticketDto.getSubject());
        ticket.setMessage(ticketDto.getMessage());
        ticket.setStatus(ticketDto.getStatus());

        SupportTicket updatedTicket = repository.save(ticket);
        return mapper.toDto(updatedTicket);
    }

    @Override
    public void deleteTicket(Long id) {
        SupportTicket ticket = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Support ticket not found with id: " + id));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        Long currentUserId = ((AuthenticatedUser) principal).getId();

        boolean isSysAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("Admin"::equals);

        if (!isSysAdmin && !currentUserId.equals(ticket.getUserId())) {
            throw new AccessDeniedException("You are not authorized to delete this ticket.");
        }

        repository.delete(ticket);
    }
}
