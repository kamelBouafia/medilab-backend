package com.medilab.controller;

import com.medilab.dto.RequisitionDto;
import com.medilab.dto.TestResultDto;
import com.medilab.service.PatientDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/patient")
@PreAuthorize("hasRole('Patient')")
@RequiredArgsConstructor
public class PatientEndpointsController {

    private final PatientDataService patientDataService;

    @GetMapping("/requisitions")
    public ResponseEntity<List<RequisitionDto>> getPatientRequisitions() {
        return ResponseEntity.ok(patientDataService.getPatientRequisitions());
    }

    @GetMapping("/requisitions/{reqId}/results")
    public ResponseEntity<List<TestResultDto>> getPatientTestResults(@PathVariable Long reqId) {
        return ResponseEntity.ok(patientDataService.getPatientTestResults(reqId));
    }
}
