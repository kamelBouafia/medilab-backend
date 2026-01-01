package com.medilab.service.impl;

import com.medilab.dto.SupportContactRequest;
import com.medilab.dto.SupportContactResponse;
import com.medilab.dto.SupportTicketDto;
import com.medilab.entity.SupportTicket;
import com.medilab.exception.ResourceNotFoundException;
import com.medilab.mapper.SupportTicketMapper;
import com.medilab.repository.SupportTicketRepository;
import com.medilab.security.AuthenticatedUser;
import com.medilab.dto.NotificationRequestDTO;
import com.medilab.service.NotificationProducerService;
import com.medilab.repository.LabRepository;
import com.medilab.service.SupportService;
import com.medilab.entity.Lab;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequiredArgsConstructor
public class SupportServiceImpl implements SupportService {

    private static final Logger log = LoggerFactory.getLogger(SupportServiceImpl.class);

    private final SupportTicketRepository repository;
    private final SupportTicketMapper mapper;
    private final NotificationProducerService notificationProducerService;
    private final LabRepository labRepository;

    private static final String SYSTEM_ADMIN_EMAIL = "ak_bouafia@esi.dz";

    @Override
    public SupportContactResponse createSupportTicket(SupportContactRequest request) {
        String ticketId = "TKT-" + UUID.randomUUID();
        SupportTicket.SupportTicketBuilder ticketBuilder = SupportTicket.builder()
                .ticketId(ticketId)
                .subject(request.getSubject())
                .message(request.getMessage())
                .status("created")
                .createdAt(OffsetDateTime.now());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser) {
            ticketBuilder.userId(authenticatedUser.getId())
                    .labId(authenticatedUser.getLabId())
                    .name(authenticatedUser.getUsername());
        } else {
            ticketBuilder.name(request.getName())
                    .email(request.getEmail())
                    .labId(request.getLabId());
        }

        SupportTicket ticket = ticketBuilder.build();
        log.info("Creating support ticket {} for labId={} userId={}: {} - {}",
                ticket.getTicketId(), ticket.getLabId(), ticket.getUserId(), ticket.getSubject(), ticket.getMessage());

        SupportTicket saved = repository.save(ticket);

        sendSupportNotification(saved);

        return new SupportContactResponse(saved.getTicketId(), saved.getStatus());
    }

    private void sendSupportNotification(SupportTicket ticket) {
        try {
            String recipient = SYSTEM_ADMIN_EMAIL;
            Authentication authentication = getAuthentication();

            if (authentication != null
                    && authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser) {
                if ("staff".equalsIgnoreCase(authenticatedUser.getUserType())) {
                    // Staff/Manager -> System Admin
                    recipient = SYSTEM_ADMIN_EMAIL;
                } else if ("patient".equalsIgnoreCase(authenticatedUser.getUserType())) {
                    // Patient -> Lab Manager
                    recipient = getLabContactEmail(ticket.getLabId());
                }
            } else {
                // Anonymous -> Lab Manager or System Admin (depending on if labId is provided)
                if (ticket.getLabId() != null) {
                    recipient = getLabContactEmail(ticket.getLabId());
                }
            }

            NotificationRequestDTO notification = NotificationRequestDTO.builder()
                    .recipient(recipient)
                    .subject("New Support Ticket: " + ticket.getTicketId())
                    .content("A new support ticket has been created.\n\n" +
                            "From: " + ticket.getName() + " ("
                            + (ticket.getEmail() != null ? ticket.getEmail() : "Authenticated User") + ")\n" +
                            "Subject: " + ticket.getSubject() + "\n" +
                            "Message: " + ticket.getMessage())
                    .type("EMAIL")
                    .build();
            notificationProducerService.sendNotification(notification);
        } catch (Exception e) {
            log.error("Failed to queue notification for ticket {}", ticket.getTicketId(), e);
        }
    }

    private String getLabContactEmail(Long labId) {
        if (labId == null)
            return SYSTEM_ADMIN_EMAIL;
        return labRepository.findById(labId)
                .map(Lab::getContactEmail)
                .filter(email -> email != null && !email.isBlank())
                .orElse(SYSTEM_ADMIN_EMAIL);
    }

    @Override
    public Page<SupportTicketDto> searchSupportTickets(String q, Long labId, String status, Long userId,
            Pageable pageable) {
        Authentication authentication = getAuthentication();

        String q1 = (q == null || q.isBlank()) ? null : q;
        String status1 = (status == null || status.isBlank()) ? null : status;

        if (isSysAdmin(authentication)) {
            return repository.search(q1, labId, status1, userId, pageable).map(mapper::toDto);
        }

        AuthenticatedUser authenticatedUser = getAuthenticatedUser(authentication);

        if ("patient".equals(authenticatedUser.getUserType())) {
            return repository.search(q1, authenticatedUser.getLabId(), status1, authenticatedUser.getId(), pageable)
                    .map(mapper::toDto);
        } else {
            return repository.search(q1, authenticatedUser.getLabId(), status1, null, pageable).map(mapper::toDto);
        }
    }

    @Override
    public SupportTicketDto getTicketById(Long id) {
        SupportTicket ticket = findTicketById(id);
        authorizeViewAccess(ticket);
        return mapper.toDto(ticket);
    }

    @Override
    public SupportTicketDto updateTicket(Long id, SupportTicketDto ticketDto) {
        SupportTicket ticket = findTicketById(id);
        authorizeModification(ticket);

        ticket.setSubject(ticketDto.getSubject());
        ticket.setMessage(ticketDto.getMessage());
        ticket.setStatus(ticketDto.getStatus());

        SupportTicket updatedTicket = repository.save(ticket);
        return mapper.toDto(updatedTicket);
    }

    @Override
    public void deleteTicket(Long id) {
        SupportTicket ticket = findTicketById(id);
        authorizeModification(ticket);
        repository.delete(ticket);
    }

    private SupportTicket findTicketById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Support ticket not found with id: " + id));
    }

    private void authorizeViewAccess(SupportTicket ticket) {
        Authentication authentication = getAuthentication();
        if (isSysAdmin(authentication)) {
            return; // System admins can access any ticket
        }

        AuthenticatedUser authenticatedUser = getAuthenticatedUser(authentication);

        if ("patient".equals(authenticatedUser.getUserType())) {
            if (!authenticatedUser.getId().equals(ticket.getUserId())) {
                throw new AccessDeniedException("You are not authorized to perform this action on this ticket.");
            }
        } else { // staff
            if (!authenticatedUser.getLabId().equals(ticket.getLabId())) {
                throw new AccessDeniedException("You are not authorized to perform this action on this ticket.");
            }
        }
    }

    private void authorizeModification(SupportTicket ticket) {
        Authentication authentication = getAuthentication();
        if (isSysAdmin(authentication)) {
            return; // System admins can modify any ticket
        }

        AuthenticatedUser authenticatedUser = getAuthenticatedUser(authentication);
        // For all other users (staff or patient), they must be the owner to modify.
        if (!authenticatedUser.getId().equals(ticket.getUserId())) {
            throw new AccessDeniedException("You are not authorized to perform this action on this ticket.");
        }
    }

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private AuthenticatedUser getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser)) {
            throw new AccessDeniedException("User not authenticated.");
        }
        return (AuthenticatedUser) authentication.getPrincipal();
    }

    private boolean isSysAdmin(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("SYSTEM_ADMIN"::equals);
    }
}
