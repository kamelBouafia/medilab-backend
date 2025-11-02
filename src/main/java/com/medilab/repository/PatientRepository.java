package com.medilab.repository;

import com.medilab.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    List<Patient> findByLabId(Long labId);
    Optional<Patient> findByLabIdAndIdAndDob(Long labId, Long id, LocalDate dob);

    Optional<Patient> findByLabIdAndId(Long labId, Long id);
}
