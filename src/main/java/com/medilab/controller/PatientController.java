package com.medilab.controller;

import com.medilab.entity.Patient;
import com.medilab.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    @Autowired
    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    public ResponseEntity<List<Patient>> getPatients() {
        // Assuming a service method that returns all patients
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    @PostMapping
    public ResponseEntity<Patient> addPatient(@RequestBody Patient patient) {
        // Assuming a service method that saves a new patient
        return ResponseEntity.ok(patientService.addPatient(patient));
    }
}
