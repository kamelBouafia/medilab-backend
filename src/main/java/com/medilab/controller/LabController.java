package com.medilab.controller;

import com.medilab.dto.LabDto;
import com.medilab.entity.Lab;
import com.medilab.exception.ResourceNotFoundException;
import com.medilab.repository.LabRepository;
import com.medilab.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import com.medilab.security.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/labs")
@RequiredArgsConstructor
public class LabController {

    private final LabRepository labRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'Manager')")
    public ResponseEntity<java.util.List<LabDto>> getAllLabs() {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
        boolean isSysAdmin = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("SYSTEM_ADMIN"));

        java.util.List<Lab> labs;
        if (isSysAdmin) {
            labs = labRepository.findAll();
        } else {
            // Manager: see own lab and branch labs
            labs = labRepository.findAll().stream()
                    .filter(l -> l.getId().equals(user.getLabId())
                            || (l.getParentLab() != null && l.getParentLab().getId().equals(user.getLabId())))
                    .collect(java.util.stream.Collectors.toList());
        }

        return ResponseEntity.ok(labs.stream()
                .map(lab -> {
                    java.time.LocalDateTime effectiveTrialEnd = lab.getTrialEnd();
                    if (lab.getParentLab() != null && effectiveTrialEnd == null) {
                        effectiveTrialEnd = lab.getParentLab().getTrialEnd();
                    }
                    return LabDto.builder()
                            .id(lab.getId())
                            .name(lab.getName())
                            .location(lab.getLocation())
                            .contactEmail(lab.getContactEmail())
                            .licenseNumber(lab.getLicenseNumber())
                            .trialStart(lab.getTrialStart())
                            .trialEnd(effectiveTrialEnd)
                            .defaultLanguage(lab.getDefaultLanguage())
                            .parentLabId(lab.getParentLab() != null ? lab.getParentLab().getId() : null)
                            .build();
                })
                .collect(java.util.stream.Collectors.toList()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SYSTEM_ADMIN', 'Manager')")
    public ResponseEntity<LabDto> createLab(@RequestBody LabDto dto) {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
        boolean isSysAdmin = user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("SYSTEM_ADMIN"));

        Long parentLabId = dto.getParentLabId();

        if (!isSysAdmin) {
            // Manager: can only create branch labs for their own lab
            if (user.getParentLabId() != null) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "Branch managers cannot create sub-branches.");
            }
            parentLabId = user.getLabId();
        }

        Lab lab = Lab.builder()
                .name(dto.getName())
                .location(dto.getLocation())
                .contactEmail(dto.getContactEmail())
                .licenseNumber(dto.getLicenseNumber())
                .trialStart(java.time.LocalDateTime.now())
                .trialEnd(isSysAdmin ? dto.getTrialEnd() : null)
                .defaultLanguage(dto.getDefaultLanguage() != null ? dto.getDefaultLanguage() : "en")
                .build();

        if (parentLabId != null) {
            Lab parent = labRepository.findById(parentLabId)
                    .orElseThrow(() -> new ResourceNotFoundException("Parent lab not found"));
            if (parent.getParentLab() != null) {
                throw new IllegalArgumentException("A branch lab cannot be a parent lab.");
            }
            lab.setParentLab(parent);
        }

        Lab saved = labRepository.save(lab);
        java.time.LocalDateTime effectiveTrialEnd = saved.getTrialEnd();
        if (saved.getParentLab() != null && effectiveTrialEnd == null) {
            effectiveTrialEnd = saved.getParentLab().getTrialEnd();
        }
        return ResponseEntity.ok(LabDto.builder()
                .id(saved.getId())
                .name(saved.getName())
                .contactEmail(saved.getContactEmail())
                .licenseNumber(saved.getLicenseNumber())
                .trialStart(saved.getTrialStart())
                .trialEnd(effectiveTrialEnd)
                .defaultLanguage(saved.getDefaultLanguage())
                .parentLabId(saved.getParentLab() != null ? saved.getParentLab().getId() : null)
                .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<LabDto> updateLab(@PathVariable Long id, @RequestBody LabDto dto) {
        Lab lab = labRepository.findById(id).orElse(null);
        if (lab == null)
            return ResponseEntity.notFound().build();

        lab.setName(dto.getName());
        lab.setLocation(dto.getLocation());
        lab.setContactEmail(dto.getContactEmail());
        lab.setLicenseNumber(dto.getLicenseNumber());
        lab.setTrialEnd(dto.getTrialEnd());
        if (dto.getDefaultLanguage() != null) {
            lab.setDefaultLanguage(dto.getDefaultLanguage());
        }

        if (dto.getParentLabId() != null) {
            if (dto.getParentLabId().equals(id)) {
                throw new IllegalArgumentException("A laboratory cannot be its own parent.");
            }
            Lab parent = labRepository.findById(dto.getParentLabId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent lab not found"));
            if (parent.getParentLab() != null) {
                throw new IllegalArgumentException("A branch lab cannot be a parent lab.");
            }
            if (labRepository.existsByParentLabId(id)) {
                throw new IllegalArgumentException("A laboratory with branch labs cannot become a branch lab.");
            }
            lab.setParentLab(parent);
        } else {
            lab.setParentLab(null);
        }

        Lab saved = labRepository.save(lab);
        java.time.LocalDateTime effectiveTrialEnd = saved.getTrialEnd();
        if (saved.getParentLab() != null && effectiveTrialEnd == null) {
            effectiveTrialEnd = saved.getParentLab().getTrialEnd();
        }
        return ResponseEntity.ok(LabDto.builder()
                .id(saved.getId())
                .name(saved.getName())
                .location(saved.getLocation())
                .contactEmail(saved.getContactEmail())
                .licenseNumber(saved.getLicenseNumber())
                .trialStart(saved.getTrialStart())
                .trialEnd(effectiveTrialEnd)
                .defaultLanguage(saved.getDefaultLanguage())
                .parentLabId(saved.getParentLab() != null ? saved.getParentLab().getId() : null)
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
        java.time.LocalDateTime effectiveTrialEnd = saved.getTrialEnd();
        if (saved.getParentLab() != null && effectiveTrialEnd == null) {
            effectiveTrialEnd = saved.getParentLab().getTrialEnd();
        }
        return ResponseEntity.ok(LabDto.builder()
                .id(saved.getId())
                .name(saved.getName())
                .location(saved.getLocation())
                .contactEmail(saved.getContactEmail())
                .licenseNumber(saved.getLicenseNumber())
                .trialStart(saved.getTrialStart())
                .trialEnd(effectiveTrialEnd)
                .defaultLanguage(saved.getDefaultLanguage())
                .build());
    }

    @GetMapping("/me")
    @Transactional(readOnly = true)
    public ResponseEntity<LabDto> getMyLab() {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
        Long labId = user.getLabId();
        Lab lab = labRepository.findById(labId).orElse(null);
        if (lab == null)
            return ResponseEntity.notFound().build();

        java.time.LocalDateTime effectiveTrialEnd = lab.getTrialEnd();
        if (lab.getParentLab() != null && effectiveTrialEnd == null) {
            effectiveTrialEnd = lab.getParentLab().getTrialEnd();
        }

        LabDto dto = LabDto.builder()
                .id(lab.getId())
                .name(lab.getName())
                .location(lab.getLocation())
                .contactEmail(lab.getContactEmail())
                .licenseNumber(lab.getLicenseNumber())
                .trialStart(lab.getTrialStart())
                .trialEnd(effectiveTrialEnd)
                .defaultLanguage(lab.getDefaultLanguage())
                .parentLabId(lab.getParentLab() != null ? lab.getParentLab().getId() : null)
                .build();

        return ResponseEntity.ok(dto);
    }
}
