package com.medilab.controller;

import com.medilab.dto.AuditLogDto;
import com.medilab.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit-log")
@PreAuthorize("hasRole('Manager')")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<Page<AuditLogDto>> getAuditTrail(
            @RequestParam(defaultValue = "0") int _page,
            @RequestParam(defaultValue = "10") int _limit,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "timestamp") String _sort,
            @RequestParam(defaultValue = "desc") String _order) {
        Page<AuditLogDto> auditLogPage = auditLogService.getAuditTrail(_page, _limit, q, _sort, _order);
        return ResponseEntity.ok(auditLogPage);
    }
}
