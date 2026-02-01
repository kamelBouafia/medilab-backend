package com.medilab.repository;

import com.medilab.entity.InterLabAgreement;
import com.medilab.enums.AgreementStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface InterLabAgreementRepository extends JpaRepository<InterLabAgreement, Long> {

        Page<InterLabAgreement> findByMainLabIdAndStatus(Long mainLabId, AgreementStatus status, Pageable pageable);

        Page<InterLabAgreement> findByPartnerLabIdAndStatus(Long partnerLabId, AgreementStatus status,
                        Pageable pageable);

        @Query("SELECT a FROM InterLabAgreement a WHERE (a.mainLab.id = :labId OR a.partnerLab.id = :labId)")
        Page<InterLabAgreement> findByLabId(@Param("labId") Long labId, Pageable pageable);

        @Query("SELECT a FROM InterLabAgreement a WHERE (a.mainLab.id = :labId OR a.partnerLab.id = :labId) AND a.status = :status")
        Page<InterLabAgreement> findByLabIdAndStatus(@Param("labId") Long labId,
                        @Param("status") AgreementStatus status,
                        Pageable pageable);

        @Query("SELECT a FROM InterLabAgreement a WHERE a.mainLab.id = :mainLabId AND a.partnerLab.id = :partnerLabId AND a.status = :status AND (a.validTo IS NULL OR a.validTo > :now)")
        Optional<InterLabAgreement> findActiveAgreementBetweenLabs(
                        @Param("mainLabId") Long mainLabId,
                        @Param("partnerLabId") Long partnerLabId,
                        @Param("status") AgreementStatus status,
                        @Param("now") LocalDateTime now);

        @Query("SELECT DISTINCT tp.labTest.id FROM InterLabAgreement a JOIN a.testPrices tp " +
                        "WHERE ((a.mainLab.id = :mainLabId AND a.partnerLab.id = :partnerLabId) " +
                        "OR (a.mainLab.id = :partnerLabId AND a.partnerLab.id = :mainLabId)) " +
                        "AND a.status IN ('PENDING', 'COUNTER_OFFER', 'APPROVED')")
        java.util.List<Long> findTestIdsWithActiveAgreements(
                        @Param("mainLabId") Long mainLabId,
                        @Param("partnerLabId") Long partnerLabId);
}
