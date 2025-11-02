package com.medilab.service;

import com.medilab.dto.AuditLogDto;
import com.medilab.entity.AuditLog;
import com.medilab.mapper.AuditLogMapper;
import com.medilab.repository.AuditLogRepository;
import com.medilab.repository.LabRepository;
import com.medilab.repository.StaffUserRepository;
import com.medilab.security.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private StaffUserRepository staffUserRepository;

    @Autowired
    private LabRepository labRepository;

    @Autowired
    private AuditLogMapper auditLogMapper;

    public List<AuditLogDto> getAuditTrail() {
        AuthenticatedUser user = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return auditLogRepository.findByLabIdOrderByTimestampDesc(user.getLabId()).stream()
                .map(auditLogMapper::toDto)
                .collect(Collectors.toList());
    }

    public void logAction(String action, String details) {
        AuthenticatedUser user = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setDetails(details);

        staffUserRepository.findById(user.getId()).ifPresent(auditLog::setUser);
        labRepository.findById(user.getLabId()).ifPresent(auditLog::setLab);

        auditLogRepository.save(auditLog);
    }
}
