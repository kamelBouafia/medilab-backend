package com.medilab.controller;

import com.medilab.dto.LabDto;
import com.medilab.entity.Lab;
import com.medilab.repository.LabRepository;
import com.medilab.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import com.medilab.security.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/labs")
@RequiredArgsConstructor
public class LabController {

    private final LabRepository labRepository;

    @GetMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<java.util.List<LabDto>> getAllLabs() {
        return ResponseEntity.ok(labRepository.findAll().stream()
                .map(lab -> LabDto.builder()
                        .id(lab.getId())
                        .name(lab.getName())
                        .location(lab.getLocation())
                        .contactEmail(lab.getContactEmail())
                        .licenseNumber(lab.getLicenseNumber())
                        .trialStart(lab.getTrialStart())
                        .trialEnd(lab.getTrialEnd())
                        .build())
                .collect(java.util.stream.Collectors.toList()));
    }

    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<LabDto> createLab(@RequestBody Lab lab) {
        Lab saved = labRepository.save(lab);
        return ResponseEntity.ok(LabDto.builder()
                .id(saved.getId())
                .name(saved.getName())
                .contactEmail(saved.getContactEmail())
                .licenseNumber(saved.getLicenseNumber())
                .trialStart(saved.getTrialStart())
                .trialEnd(saved.getTrialEnd())
                .build());
    }

    @GetMapping("/me")
    public ResponseEntity<LabDto> getMyLab() {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
        Long labId = user.getLabId();
        Lab lab = labRepository.findById(labId).orElse(null);
        if (lab == null)
            return ResponseEntity.notFound().build();

        LabDto dto = LabDto.builder()
                .id(lab.getId())
                .name(lab.getName())
                .location(lab.getLocation())
                .contactEmail(lab.getContactEmail())
                .licenseNumber(lab.getLicenseNumber())
                .trialStart(lab.getTrialStart())
                .trialEnd(lab.getTrialEnd())
                .build();

        return ResponseEntity.ok(dto);
    }
}
