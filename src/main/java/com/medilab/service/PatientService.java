package com.medilab.service;

import com.medilab.entity.Patient;
import com.medilab.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    @Autowired
    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public Patient addPatient(Patient patient) {
        // In a real application, you would have more logic here
        // for validation, setting createdById, etc.
        return patientRepository.save(patient);
    }

    public List<Patient> findAllByLab(String labId) {
        return patientRepository.findAllByLabId(labId);
    }

    public Patient save(Patient patient) {
        return patientRepository.save(patient);
    }
}
