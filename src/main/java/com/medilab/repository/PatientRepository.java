package com.medilab.repository;

import com.medilab.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long>, JpaSpecificationExecutor<Patient> {
    Optional<Patient> findByUsername(String username);

    Optional<Patient> findByIdAndLabId(Long id, Long labId);

    @org.springframework.data.jpa.repository.Query("SELECT p FROM Patient p WHERE p.id = :id AND (p.lab.id = :labId OR p.lab.parentLab.id = :labId)")
    Optional<Patient> findByIdAndHierarchicalLabId(Long id, Long labId);
}
