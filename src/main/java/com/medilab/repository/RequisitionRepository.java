package com.medilab.repository;

import com.medilab.entity.Requisition;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RequisitionRepository extends JpaRepository<Requisition, String> {
    List<Requisition> findAllByLabId(String labId);
    List<Requisition> findAllByPatientIdAndLabId(String patientId, String labId);

    List<Requisition> findAllByPatientId(String patientId);
}
