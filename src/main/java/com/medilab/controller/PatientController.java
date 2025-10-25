package com.medilab.controller;

import com.medilab.config.TenantPrincipal;
import com.medilab.entity.Requisition;
import com.medilab.entity.TestResult;
import com.medilab.repository.RequisitionRepository;
import com.medilab.repository.TestResultRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

    private final RequisitionRepository requisitionRepository;
    private final TestResultRepository testResultRepository;

    public PatientController(RequisitionRepository requisitionRepository, TestResultRepository testResultRepository) {
        this.requisitionRepository = requisitionRepository;
        this.testResultRepository = testResultRepository;
    }

    @GetMapping("/requisitions")
    public ResponseEntity<List<Requisition>> myRequisitions(Authentication authentication) {

        Object detailsObj = authentication.getDetails();
        String patientId = null;

        if (detailsObj instanceof Map<?, ?> detailsMap) {
            patientId = (String) detailsMap.get("patientId");
        }

        if (patientId == null) {
            return ResponseEntity.status(401).build(); // Unauthorized
        }

        // Fetch all requisitions for this patient
        List<Requisition> requisitions = requisitionRepository.findAllByPatientId(patientId);

        return ResponseEntity.ok(requisitions);
    }

    @GetMapping("/requisitions/{reqId}/results")
    public ResponseEntity<?> results(@PathVariable String reqId, Authentication auth) {
        TenantPrincipal tp = (TenantPrincipal) auth;
        List<TestResult> results = testResultRepository.findAllByRequisitionIdAndLabId(reqId, tp.getLabId());
        return ResponseEntity.ok(results);
    }
}
