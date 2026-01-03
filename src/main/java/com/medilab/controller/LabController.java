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
                        .defaultLanguage(lab.getDefaultLanguage())
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
                .defaultLanguage(saved.getDefaultLanguage())
                .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<LabDto> updateLab(@PathVariable Long id, @RequestBody Lab labDetails) {
        Lab lab = labRepository.findById(id).orElse(null);
        if (lab == null)
            return ResponseEntity.notFound().build();

        lab.setName(labDetails.getName());
        lab.setLocation(labDetails.getLocation());
        lab.setContactEmail(labDetails.getContactEmail());
        lab.setLicenseNumber(labDetails.getLicenseNumber());
        lab.setTrialEnd(labDetails.getTrialEnd());
        if (labDetails.getDefaultLanguage() != null) {
            lab.setDefaultLanguage(labDetails.getDefaultLanguage());
        }

        Lab saved = labRepository.save(lab);
        return ResponseEntity.ok(LabDto.builder()
                .id(saved.getId())
                .name(saved.getName())
                .location(saved.getLocation())
                .contactEmail(saved.getContactEmail())
                .licenseNumber(saved.getLicenseNumber())
                .trialStart(saved.getTrialStart())
                .trialEnd(saved.getTrialEnd())
                .defaultLanguage(saved.getDefaultLanguage())
                .build());
    }

    @PatchMapping("/me")
    @PreAuthorize("hasRole('Manager')")
    public ResponseEntity<LabDto> updateMyLab(@RequestBody LabDto dto) {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
        Lab lab = labRepository.findById(user.getLabId()).orElse(null);
        if (lab == null)
            return ResponseEntity.notFound().build();

        if (dto.getName() != null)
            lab.setName(dto.getName());
        if (dto.getLocation() != null)
            lab.setLocation(dto.getLocation());
        if (dto.getContactEmail() != null)
            lab.setContactEmail(dto.getContactEmail());
        if (dto.getDefaultLanguage() != null)
            lab.setDefaultLanguage(dto.getDefaultLanguage());

        Lab saved = labRepository.save(lab);
        return ResponseEntity.ok(LabDto.builder()
                .id(saved.getId())
                .name(saved.getName())
                .location(saved.getLocation())
                .contactEmail(saved.getContactEmail())
                .licenseNumber(saved.getLicenseNumber())
                .trialStart(saved.getTrialStart())
                .trialEnd(saved.getTrialEnd())
                .defaultLanguage(saved.getDefaultLanguage())
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
                .defaultLanguage(lab.getDefaultLanguage())
                .build();

        return ResponseEntity.ok(dto);
    }
}
