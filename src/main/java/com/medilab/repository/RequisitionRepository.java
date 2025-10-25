package com.medilab.repository;

import com.medilab.entity.Requisition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequisitionRepository extends JpaRepository<Requisition, Long> {
    List<Requisition> findByLabId(Long labId);
    List<Requisition> findByPatientIdAndLabId(Long patientId, Long labId);
}
