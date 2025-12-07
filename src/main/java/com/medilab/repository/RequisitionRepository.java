package com.medilab.repository;

import com.medilab.entity.Requisition;
import com.medilab.entity.SampleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RequisitionRepository extends JpaRepository<Requisition, Long>, JpaSpecificationExecutor<Requisition> {
    List<Requisition> findByLabId(Long labId);
    List<Requisition> findByPatientIdAndLabId(Long patientId, Long labId);

    @Query("SELECT r FROM Requisition r LEFT JOIN FETCH r.tests LEFT JOIN FETCH r.testResults WHERE r.patient.id = :patientId AND r.lab.id = :labId")
    Page<Requisition> findByPatientIdAndLabIdWithTestsAndResults(Long patientId, Long labId, Pageable pageable);

    @Query("SELECT r FROM Requisition r LEFT JOIN FETCH r.tests WHERE r.patient.id = :patientId AND r.lab.id = :labId")
    List<Requisition> findByPatientIdAndLabIdWithTests(Long patientId, Long labId);

    @Query("SELECT r FROM Requisition r LEFT JOIN FETCH r.tests WHERE r.lab.id = :labId")
    List<Requisition> findByLabIdWithTests(Long labId);

    @Query("SELECT r FROM Requisition r LEFT JOIN FETCH r.tests LEFT JOIN FETCH r.testResults WHERE r.id = :id AND r.lab.id = :labId")
    Optional<Requisition> findByIdAndLabIdWithTestsAndResults(Long id, Long labId);

    long countByLabIdAndStatus(Long labId, SampleStatus status);

    long countByLabIdAndStatusIn(Long labId, Collection<SampleStatus> statuses);

    long countByLabIdAndStatusAndCompletionDateBetween(Long labId, SampleStatus status, LocalDateTime start, LocalDateTime end);
}
