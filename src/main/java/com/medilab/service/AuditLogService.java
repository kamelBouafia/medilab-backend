package com.medilab.service;

import com.medilab.dto.AuditLogDto;
import com.medilab.entity.AuditLog;
import com.medilab.mapper.AuditLogMapper;
import com.medilab.repository.AuditLogRepository;
import com.medilab.repository.LabRepository;
import com.medilab.repository.StaffUserRepository;
import com.medilab.security.AuthenticatedUser;
import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import com.medilab.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final StaffUserRepository staffUserRepository;
    private final LabRepository labRepository;
    private final AuditLogMapper auditLogMapper;

    public Page<AuditLogDto> getAuditTrail(int page, int limit, String q, String sort, String order) {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
        Sort.Direction direction = Sort.Direction.fromString(order);
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(direction, sort));

        Specification<AuditLog> spec = (root, query, cb) -> {
            if (query.getResultType() != Long.class) {
                root.fetch("user", JoinType.LEFT);
            }
            return cb.equal(root.get("lab").get("id"), user.getLabId());
        };

        if (StringUtils.hasText(q)) {
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("action")), "%" + q.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("details")), "%" + q.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("user").get("username")), "%" + q.toLowerCase() + "%")));
        }

        return auditLogRepository.findAll(spec, pageable).map(auditLogMapper::toDto);
    }

    public void logAction(String action, String details) {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
        logAction(user, action, details);
    }

    public void logAction(AuthenticatedUser user, String action, String details) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setDetails(details);

        staffUserRepository.findById(user.getId()).ifPresent(auditLog::setUser);
        if (user.getLabId() != null) {
            labRepository.findById(user.getLabId()).ifPresent(auditLog::setLab);
        }

        auditLogRepository.save(auditLog);
    }
}
