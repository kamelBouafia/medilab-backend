package com.medilab.repository;

import com.medilab.entity.LabTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LabTestRepository extends JpaRepository<LabTest, Long> {
    List<LabTest> findByLabId(Long labId);

    Optional<LabTest> findByIdAndLabId(Long id, Long labId);
}
