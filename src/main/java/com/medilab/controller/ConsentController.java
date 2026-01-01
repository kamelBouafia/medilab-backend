package com.medilab.controller;

import com.medilab.entity.Patient;
import com.medilab.entity.StaffUser;
import com.medilab.repository.PatientRepository;
import com.medilab.repository.StaffUserRepository;
import com.medilab.security.AuthenticatedUser;
import com.medilab.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/consent")
@RequiredArgsConstructor
public class ConsentController {

    private final StaffUserRepository staffUserRepository;
    private final PatientRepository patientRepository;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<?> acceptConsent(@AuthenticationPrincipal AuthenticatedUser user) {
        if ("staff".equals(user.getUserType())) {
            StaffUser staffUser = staffUserRepository.findById(user.getId())
                    .orElseThrow(() -> new RuntimeException("Staff user not found"));
            staffUser.setGdprAccepted(true);
            staffUser.setGdprAcceptedAt(LocalDateTime.now());
            staffUserRepository.save(staffUser);
        } else if ("patient".equals(user.getUserType())) {
            Patient patient = patientRepository.findById(user.getId())
                    .orElseThrow(() -> new RuntimeException("Patient not found"));
            patient.setGdprAccepted(true);
            patient.setGdprAcceptedAt(LocalDateTime.now());
            patientRepository.save(patient);
        }

        // Return a fresh token with updated claims
        AuthenticatedUser updatedUser = new AuthenticatedUser(
                user.getId(),
                user.getLabId(),
                user.getUsername(),
                user.getPassword(),
                user.getAuthorities(),
                user.getUserType(),
                user.isForcePasswordChange(),
                true,
                user.isEnabled());

        String newToken = jwtUtil.generateToken(updatedUser);
        return ResponseEntity.ok(Map.of("token", newToken));
    }
}
