package com.medilab.repository;

import com.medilab.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    List<Patient> findByLabId(Long labId);
    Optional<Patient> findByUsername(String username);
    Optional<Patient> findByIdAndLabId(Long id, Long labId);
}
