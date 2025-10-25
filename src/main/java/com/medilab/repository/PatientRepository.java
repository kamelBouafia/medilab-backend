package com.medilab.repository;

import com.medilab.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PatientRepository extends JpaRepository<Patient, String> {
    List<Patient> findAllByLabId(String labId);
}
