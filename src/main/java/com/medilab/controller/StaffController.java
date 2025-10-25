package com.medilab.controller;

import com.medilab.dto.StaffUserDto;
import com.medilab.entity.StaffUser;
import com.medilab.mapper.StaffUserMapper;
import com.medilab.service.AuditService;
import com.medilab.service.PatientService;
import com.medilab.service.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;
    private final StaffUserMapper staffUserMapper;

    @GetMapping("/staff-init/{labId}")
    public ResponseEntity<List<StaffUserDto>> staffInit(@PathVariable Long labId) {
        List<StaffUser> staffList = staffService.findAllByLab(labId);
        List<StaffUserDto> dtoList = staffUserMapper.toDTOs(staffList);
        return ResponseEntity.ok(dtoList);
    }

//    @GetMapping("/patients")
//    public ResponseEntity<?> allPatients(Authentication auth) {
//        TenantPrincipal p = (TenantPrincipal) auth;
//        List<Patient> patients = patientService.findAllByLab(p.getLabId());
//        return ResponseEntity.ok(patients);
//    }
//
//    @PostMapping("/patients")
//    public ResponseEntity<?> createPatient(@RequestBody Map<String, String> body, Authentication auth) {
//        TenantPrincipal tp = (TenantPrincipal) auth;
//        String name = body.get("name");
//        String dob = body.get("dob"); // yyyy-mm-dd
//        String gender = body.get("gender");
//        String contact = body.get("contact");
//        Patient patient = Patient.builder()
//                .id(UUID.randomUUID().toString())
//                .name(name)
//                .dob(java.time.LocalDate.parse(dob))
//                .gender(Patient.Gender.valueOf(gender))
//                .contact(contact)
//                .createdById(tp.getUserId())
//                .labId(tp.getLabId())
//                .build();
//        Patient saved = patientService.save(patient);
//        auditService.log(tp.getUserId(), "Add Patient", "Added patient: " + saved.getName() + " (ID:" + saved.getId() + ")", tp.getLabId());
//        return ResponseEntity.status(201).body(saved);
//    }
}
