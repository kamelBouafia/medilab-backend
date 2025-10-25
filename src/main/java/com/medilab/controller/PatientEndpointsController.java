package com.medilab.controller;

import com.medilab.entity.RequisitionTest;
import com.medilab.entity.TestResult;
import com.medilab.service.PatientDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/patient")
public class PatientEndpointsController {

    private final PatientDataService patientDataService;

    @Autowired
    public PatientEndpointsController(PatientDataService patientDataService) {
        this.patientDataService = patientDataService;
    }

    @GetMapping("/requisitions")
    public ResponseEntity<List<RequisitionTest>> getRequisitionsForPatient() {
        return ResponseEntity.ok(patientDataService.getRequisitionsForPatient());
    }

    @GetMapping("/requisitions/{requisitionId}/results")
    public ResponseEntity<List<TestResult>> getTestResults(@PathVariable String requisitionId) {
        return ResponseEntity.ok(patientDataService.getTestResults(requisitionId));
    }
}
