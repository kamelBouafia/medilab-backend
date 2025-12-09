package com.medilab.controller;

import com.medilab.dto.SupportContactRequest;
import com.medilab.dto.SupportContactResponse;
import com.medilab.dto.SupportTicketDto;
import com.medilab.service.SupportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/support")
@Validated
public class SupportController {

    private final SupportService supportService;

    @Autowired
    public SupportController(SupportService supportService) {
        this.supportService = supportService;
    }

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
            Pageable pageable) {

        boolean hasFilter = (q != null && !q.isBlank()) || labId != null || (status != null && !status.isBlank());
        Page<SupportTicketDto> page;
        if (hasFilter) {
            page = supportService.searchSupportTickets(q, labId, status, pageable);
        } else {
            page = supportService.listSupportTickets(pageable);
        }
        return ResponseEntity.ok(page);
    }

}
