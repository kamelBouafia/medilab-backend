package com.medilab.controller;

import com.medilab.dto.LabTestDto;
import com.medilab.service.LabTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lab-tests")
@RequiredArgsConstructor
public class LabTestController {

    private final LabTestService labTestService;

    @GetMapping
    @PreAuthorize("hasAnyRole('Staff', 'Patient')")
    public ResponseEntity<List<LabTestDto>> getLabTests() {
        return ResponseEntity.ok(labTestService.getLabTests());
    }
}
