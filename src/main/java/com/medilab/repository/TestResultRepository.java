package com.medilab.repository;

import com.medilab.entity.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestResultRepository extends JpaRepository<TestResult, Long> {
    List<TestResult> findByRequisitionIdAndLabId(Long requisitionId, Long labId);

    List<TestResult> findByRequisitionId(Long requisitionId);

    long countByRequisitionId(Long requisitionId);
}
