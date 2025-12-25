package com.medilab.repository;

import com.medilab.entity.LabTest;
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

    Optional<LabTest> findByGlobalTestIdAndLabId(Long globalTestId, Long labId);

    @Query("SELECT lt FROM LabTest lt LEFT JOIN FETCH lt.referenceRanges WHERE lt.id IN :ids")
    List<LabTest> findByIdInWithReferenceRanges(@Param("ids") Set<Long> ids);
}
