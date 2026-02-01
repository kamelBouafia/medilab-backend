package com.medilab.repository;

import com.medilab.entity.LabTest;
import com.medilab.enums.TestType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface LabTestRepository extends JpaRepository<LabTest, Long>, JpaSpecificationExecutor<LabTest> {

    Optional<LabTest> findByIdAndLabId(Long id, Long labId);

    @Query("SELECT lt FROM LabTest lt WHERE lt.id = :id AND (lt.lab.id = :labId OR lt.lab.parentLab.id = :labId)")
    Optional<LabTest> findByIdAndHierarchicalLabId(Long id, Long labId);

    Optional<LabTest> findByGlobalTestIdAndLabId(Long globalTestId, Long labId);

    Optional<LabTest> findByLabIdAndGlobalTestIdAndType(Long labId, Long globalTestId, TestType type);

    Optional<LabTest> findByLabIdAndCodeAndType(Long labId, String code, TestType type);

    @Query("SELECT lt FROM LabTest lt LEFT JOIN FETCH lt.referenceRanges WHERE lt.id IN :ids")
    List<LabTest> findByIdInWithReferenceRanges(@Param("ids") Set<Long> ids);

    List<LabTest> findByLabId(Long labId);
}
