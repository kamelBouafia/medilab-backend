package com.medilab.repository;

import com.medilab.entity.LabTest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LabTestRepository extends JpaRepository<LabTest, String> {
    List<LabTest> findAllByLabId(String labId);
}
