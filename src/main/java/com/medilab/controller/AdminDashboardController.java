package com.medilab.controller;

import com.medilab.dto.SystemStatsDto;
import com.medilab.repository.LabRepository;
import com.medilab.repository.PatientRepository;
import com.medilab.repository.StaffUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/stats")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final LabRepository labRepository;
    private final StaffUserRepository staffUserRepository;
    private final PatientRepository patientRepository;

    @GetMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<SystemStatsDto> getSystemStats() {
        return ResponseEntity.ok(SystemStatsDto.builder()
                .totalLabs(labRepository.count())
                .activeSubscriptions(labRepository.countByTrialEndAfter(LocalDateTime.now()))
                .totalUsers(staffUserRepository.count() + patientRepository.count())
                .systemHealth(100.0) // Real health check could be implemented later
                .build());
    }
}
