package com.medilab.service;

import com.medilab.entity.Patient;
import com.medilab.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientService {
    private final PatientRepository repo;
    public PatientService(PatientRepository repo) { this.repo = repo; }
    public List<Patient> findAllByLab(String labId){ return repo.findAllByLabId(labId); }
    public Patient save(Patient p){ return repo.save(p); }
    public java.util.Optional<Patient> findById(String id){ return repo.findById(id); }
}
