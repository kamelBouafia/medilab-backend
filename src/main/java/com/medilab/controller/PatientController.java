package com.medilab.controller;

import com.medilab.dto.PatientDto;
import com.medilab.service.PatientService;
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
    public ResponseEntity<PatientDto> createPatient(@RequestBody PatientDto patientDto) {
        return new ResponseEntity<>(patientService.createPatient(patientDto), HttpStatus.CREATED);
    }

    @PutMapping("/{patientId}")
    public ResponseEntity<PatientDto> updatePatient(@PathVariable Long patientId, @RequestBody PatientDto patientDto) {
        return ResponseEntity.ok(patientService.updatePatient(patientId, patientDto));
    }

    @DeleteMapping("/{patientId}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long patientId) {
        patientService.deletePatient(patientId);
        return ResponseEntity.noContent().build();
    }
}
