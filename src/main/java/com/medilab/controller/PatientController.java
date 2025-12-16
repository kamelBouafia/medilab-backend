package com.medilab.controller;

import com.medilab.dto.PatientDto;
import com.medilab.service.AuditLogService;
import com.medilab.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@PreAuthorize("hasRole('Staff')")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;
    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<List<PatientDto>> getPatients(
            @RequestParam(defaultValue = "0") int _page,
            @RequestParam(defaultValue = "10") int _limit,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "id") String _sort,
            @RequestParam(defaultValue = "asc") String _order) {
        Page<PatientDto> patientPage = patientService.getPatients(_page, _limit, q, _sort, _order);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(patientPage.getTotalElements()));
        return ResponseEntity.ok().headers(headers).body(patientPage.getContent());
    }

    @PostMapping
    public ResponseEntity<PatientDto> createPatient(@Valid @RequestBody PatientDto patientDto) {
        PatientDto created = patientService.createPatient(patientDto);
        auditLogService.logAction("CREATE_PATIENT", "Created patient: " + created.getName());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{patientId}")
    public ResponseEntity<PatientDto> updatePatient(@PathVariable Long patientId,
            @Valid @RequestBody PatientDto patientDto) {
        PatientDto updated = patientService.updatePatient(patientId, patientDto);
        auditLogService.logAction("UPDATE_PATIENT", "Updated patient ID: " + patientId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{patientId}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long patientId) {
        patientService.deletePatient(patientId);
        return ResponseEntity.noContent().build();
    }
}
