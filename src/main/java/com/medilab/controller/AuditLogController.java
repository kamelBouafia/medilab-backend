package com.medilab.controller;

import com.medilab.dto.AuditLogDto;
import com.medilab.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit-trail")
@PreAuthorize("hasRole('Manager')")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<List<AuditLogDto>> getAuditTrail() {
        return ResponseEntity.ok(auditLogService.getAuditTrail());
    }
}
