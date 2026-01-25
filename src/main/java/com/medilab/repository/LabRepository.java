package com.medilab.repository;

import com.medilab.entity.Lab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LabRepository extends JpaRepository<Lab, Long> {
    long countByTrialEndAfter(LocalDateTime now);

    boolean existsByParentLabId(Long parentLabId);

    List<Lab> findByIdNotAndParentLabIsNull(Long id);
}
