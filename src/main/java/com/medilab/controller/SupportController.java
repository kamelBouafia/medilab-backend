package com.medilab.controller;

import com.medilab.dto.SupportContactRequest;
import com.medilab.dto.SupportContactResponse;
import com.medilab.dto.SupportTicketDto;
import com.medilab.service.SupportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/support")
@Validated
@RequiredArgsConstructor
public class SupportController {

    private final SupportService supportService;

    @PostMapping("/contact")
    public ResponseEntity<SupportContactResponse> contactSupport(@Valid @RequestBody SupportContactRequest request) {
        SupportContactResponse response = supportService.createSupportTicket(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/tickets")
    public ResponseEntity<Page<SupportTicketDto>> listTickets(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long labId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int _page,
            @RequestParam(defaultValue = "10") int _limit,
            @RequestParam(defaultValue = "createdAt") String _sort,
            @RequestParam(defaultValue = "desc") String _order) {
        org.springframework.data.domain.Sort.Direction direction = org.springframework.data.domain.Sort.Direction
                .fromString(_order);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest
                .of(_page > 0 ? _page - 1 : 0, _limit, org.springframework.data.domain.Sort.by(direction, _sort));
        Page<SupportTicketDto> page = supportService.searchSupportTickets(q, labId, status, userId, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/tickets/{id}")
    public ResponseEntity<SupportTicketDto> getTicketById(@PathVariable Long id) {
        SupportTicketDto ticket = supportService.getTicketById(id);
        return ResponseEntity.ok(ticket);
    }

    @PutMapping("/tickets/{id}")
    public ResponseEntity<SupportTicketDto> updateTicket(@PathVariable Long id,
            @Valid @RequestBody SupportTicketDto ticketDto) {
        SupportTicketDto updatedTicket = supportService.updateTicket(id, ticketDto);
        return ResponseEntity.ok(updatedTicket);
    }

    @DeleteMapping("/tickets/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
        supportService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }

}
