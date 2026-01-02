package com.medilab.controller;

import com.medilab.entity.Lab;
import com.medilab.repository.LabRepository;
import com.medilab.security.AuthenticatedUser;
import com.medilab.service.InterpretService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/interpret")
@PreAuthorize("hasRole('Staff')")
@RequiredArgsConstructor
public class InterpretController {

    private final InterpretService interpretService;
    private final LabRepository labRepository;

    @PostMapping
    public ResponseEntity<?> getInterpretation(@RequestBody Map<String, String> body) {
        String testName = body.get("testName");
        String resultValue = body.get("resultValue");

        AuthenticatedUser authenticatedUser = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Lab lab = labRepository.findById(authenticatedUser.getLabId()).orElseThrow(() -> new RuntimeException("Lab not found"));
        String interpretation = interpretService.getInterpretation(testName, resultValue, lab.getDefaultLanguage());
        return ResponseEntity.ok(Map.of("interpretation", interpretation));
    }
}
