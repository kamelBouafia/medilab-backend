package com.medilab.controller;

import com.medilab.dto.LabTestDto;
import com.medilab.service.LabTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lab-tests")
public class LabTestController {

    @Autowired
    private LabTestService labTestService;

    @GetMapping
    @PreAuthorize("hasAnyRole('Staff', 'Patient')")
    public ResponseEntity<List<LabTestDto>> getLabTests() {
        return ResponseEntity.ok(labTestService.getLabTests());
    }
}
