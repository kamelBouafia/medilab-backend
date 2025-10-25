package com.medilab.controller;

import com.medilab.config.TenantPrincipal;
import com.medilab.dto.StaffUserDTO;
import com.medilab.entity.Patient;
import com.medilab.entity.StaffUser;
import com.medilab.mapper.StaffUserMapper;
import com.medilab.service.AuditService;
import com.medilab.service.PatientService;
import com.medilab.service.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;
    private final PatientService patientService;
    private final AuditService auditService;
    private final StaffUserMapper staffUserMapper;

    @GetMapping("/staff-init/{labId}")
    public ResponseEntity<List<StaffUserDTO>> staffInit(@PathVariable String labId) {
        List<StaffUser> staffList = staffService.findAllByLab(labId);
        List<StaffUserDTO> dtoList = staffUserMapper.toDTOs(staffList);
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/patients")
    public ResponseEntity<?> allPatients(Authentication auth) {
        TenantPrincipal p = (TenantPrincipal) auth;
        List<Patient> patients = patientService.findAllByLab(p.getLabId());
        return ResponseEntity.ok(patients);
    }

    @PostMapping("/patients")
    public ResponseEntity<?> createPatient(@RequestBody Map<String, String> body, Authentication auth) {
        TenantPrincipal tp = (TenantPrincipal) auth;
        String name = body.get("name");
        String dob = body.get("dob"); // yyyy-mm-dd
        String gender = body.get("gender");
        String contact = body.get("contact");
        Patient patient = Patient.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .dob(java.time.LocalDate.parse(dob))
                .gender(Patient.Gender.valueOf(gender))
                .contact(contact)
                .createdById(tp.getUserId())
                .labId(tp.getLabId())
                .build();
        Patient saved = patientService.save(patient);
        auditService.log(tp.getUserId(), "Add Patient", "Added patient: " + saved.getName() + " (ID:" + saved.getId() + ")", tp.getLabId());
        return ResponseEntity.status(201).body(saved);
    }
}
