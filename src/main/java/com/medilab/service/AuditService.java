package com.medilab.service;

import com.medilab.entity.AuditLog;
import com.medilab.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import java.time.OffsetDateTime;

@Service
public class AuditService {

    private final AuditLogRepository repo;

    public AuditService(AuditLogRepository repo) { this.repo = repo; }

    public void log(String userId, String action, String details, String labId) {
        AuditLog entry = AuditLog.builder()
                .timestamp(OffsetDateTime.now())
                .userId(userId)
                .action(action)
                .details(details)
                .labId(labId)
                .build();
        repo.save(entry);
    }
}
