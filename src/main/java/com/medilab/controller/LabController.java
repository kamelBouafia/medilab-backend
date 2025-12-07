package com.medilab.controller;

import com.medilab.dto.LabDto;
import com.medilab.entity.Lab;
import com.medilab.repository.LabRepository;
import com.medilab.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/labs")
@RequiredArgsConstructor
public class LabController {

    private final LabRepository labRepository;

    @GetMapping("/me")
    public ResponseEntity<LabDto> getMyLab() {
        AuthenticatedUser user = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long labId = user.getLabId();
        Lab lab = labRepository.findById(labId).orElse(null);
        if (lab == null) return ResponseEntity.notFound().build();

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

