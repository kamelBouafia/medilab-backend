package com.medilab.repository;

import com.medilab.entity.Requisition;
import com.medilab.entity.SampleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.medilab.dto.DailyVolumeDto;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface RequisitionRepository extends JpaRepository<Requisition, Long>, JpaSpecificationExecutor<Requisition> {
        List<Requisition> findByLabId(Long labId);

        Optional<Requisition> findByPatientIdAndLabId(Long patientId, Long labId);

        Optional<Requisition> findByIdAndLabId(Long id, Long labId);

        @Query("SELECT r FROM Requisition r LEFT JOIN FETCH r.tests LEFT JOIN FETCH r.testResults WHERE r.patient.id = :patientId AND r.lab.id = :labId")
        Page<Requisition> findByPatientIdAndLabIdWithTestsAndResults(Long patientId, Long labId, Pageable pageable);

        @Query("SELECT r FROM Requisition r LEFT JOIN FETCH r.tests WHERE r.patient.id = :patientId AND r.lab.id = :labId")
        List<Requisition> findByPatientIdAndLabIdWithTests(Long patientId, Long labId);

        @Query("SELECT r FROM Requisition r LEFT JOIN FETCH r.tests WHERE r.lab.id = :labId")
        List<Requisition> findByLabIdWithTests(Long labId);

        @Query("SELECT r FROM Requisition r LEFT JOIN FETCH r.tests LEFT JOIN FETCH r.testResults WHERE r.id = :id AND (r.lab.id = :labId OR r.lab.parentLab.id = :labId OR EXISTS (SELECT t FROM r.tests t WHERE t.partnerLab.id = :labId))")
        Optional<Requisition> findByIdAndHierarchicalLabIdWithTestsAndResults(Long id, Long labId);

        @Query("SELECT r FROM Requisition r LEFT JOIN FETCH r.tests LEFT JOIN FETCH r.testResults WHERE r.patient.id = :patientId AND (r.lab.id = :labId OR r.lab.parentLab.id = :labId)")
        Page<Requisition> findByPatientIdAndHierarchicalLabIdWithTestsAndResults(Long patientId, Long labId,
                        Pageable pageable);

        @Query("SELECT r FROM Requisition r WHERE r.id = :id AND (r.lab.id = :labId OR r.lab.parentLab.id = :labId)")
        Optional<Requisition> findByIdAndHierarchicalLabId(Long id, Long labId);

        @Query("SELECT COUNT(r) FROM Requisition r WHERE (r.lab.id = :labId OR r.lab.parentLab.id = :labId) AND r.status IN :statuses")
        long countByHierarchicalLabIdAndStatusIn(Long labId, Collection<SampleStatus> statuses);

        @Query("SELECT COUNT(r) FROM Requisition r WHERE (r.lab.id = :labId OR r.lab.parentLab.id = :labId) AND r.status = :status AND r.completionDate BETWEEN :start AND :end")
        long countByHierarchicalLabIdAndStatusAndCompletionDateBetween(Long labId, SampleStatus status,
                        LocalDateTime start,
                        LocalDateTime end);

        @Query("SELECT new com.medilab.dto.DailyVolumeDto(CAST(r.date AS LocalDate), COUNT(r)) " +
                        "FROM Requisition r " +
                        "WHERE (r.lab.id = :labId OR r.lab.parentLab.id = :labId) AND r.date BETWEEN :start AND :end " +
                        "GROUP BY CAST(r.date AS LocalDate) " +
                        "ORDER BY CAST(r.date AS LocalDate) ASC")
        List<DailyVolumeDto> findHierarchicalDailyRequestVolume(Long labId,
                        java.time.OffsetDateTime start, java.time.OffsetDateTime end);

        long countByLabIdAndStatus(Long labId, SampleStatus status);

        long countByLabIdAndStatusIn(Long labId, Collection<SampleStatus> statuses);

        long countByLabIdAndStatusAndCompletionDateBetween(Long labId, SampleStatus status, LocalDateTime start,
                        LocalDateTime end);

        @Query("SELECT new com.medilab.dto.DailyVolumeDto(CAST(r.date AS LocalDate), COUNT(r)) " +
                        "FROM Requisition r " +
                        "WHERE r.lab.id = :labId AND r.date BETWEEN :start AND :end " +
                        "GROUP BY CAST(r.date AS LocalDate) " +
                        "ORDER BY CAST(r.date AS LocalDate) ASC")
        List<DailyVolumeDto> findDailyRequestVolume(Long labId,
                        java.time.OffsetDateTime start, java.time.OffsetDateTime end);

        @Query("SELECT DISTINCT r FROM Requisition r JOIN r.tests t " +
                        "WHERE t.partnerLab.id = :labId " +
                        "AND (:q IS NULL OR lower(r.patient.name) LIKE lower(concat('%', :q, '%')) OR CAST(r.id AS string) LIKE concat('%', :q, '%'))")
        Page<Requisition> findIncomingRequests(Long labId, String q, Pageable pageable);

        @Query("SELECT COUNT(DISTINCT r) FROM Requisition r JOIN r.tests t LEFT JOIN r.testResults tr " +
                        "WHERE t.partnerLab.id = :labId AND (tr.status IS NULL OR tr.status NOT IN (com.medilab.enums.TestResultStatus.FINALIZED, com.medilab.enums.TestResultStatus.CANCELLED))")
        long countIncomingRequests(Long labId);
}
