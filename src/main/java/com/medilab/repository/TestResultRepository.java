package com.medilab.repository;

import com.medilab.entity.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TestResultRepository extends JpaRepository<TestResult, Long> {
    List<TestResult> findAllByRequisitionIdAndLabId(String requisitionId, String labId);
    List<TestResult> findAllByLabId(String labId);
}
