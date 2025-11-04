package com.medilab.repository;

import com.medilab.entity.Requisition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequisitionRepository extends JpaRepository<Requisition, Long> {
    List<Requisition> findByLabId(Long labId);
    List<Requisition> findByPatientIdAndLabId(Long patientId, Long labId);

    @Query("SELECT r FROM Requisition r LEFT JOIN FETCH r.tests WHERE r.patient.id = :patientId AND r.lab.id = :labId")
    List<Requisition> findByPatientIdAndLabIdWithTests(Long patientId, Long labId);

    @Query("SELECT r FROM Requisition r LEFT JOIN FETCH r.tests WHERE r.lab.id = :labId")
    List<Requisition> findByLabIdWithTests(Long labId);
}
