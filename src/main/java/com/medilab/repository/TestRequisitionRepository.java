package com.medilab.repository;

import com.medilab.entity.RequisitionTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestRequisitionRepository extends JpaRepository<RequisitionTest, Long> {
}
