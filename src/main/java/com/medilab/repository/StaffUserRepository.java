package com.medilab.repository;

import com.medilab.entity.StaffUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffUserRepository extends JpaRepository<StaffUser, Long>, JpaSpecificationExecutor<StaffUser> {
    Optional<StaffUser> findByUsernameAndEnabledTrue(String username);

    List<StaffUser> findByLabIdAndEnabledTrue(Long labId);

    @org.springframework.data.jpa.repository.Query("SELECT s FROM StaffUser s WHERE s.id = :id AND (s.lab.id = :labId OR s.lab.parentLab.id = :labId)")
    Optional<StaffUser> findByIdAndHierarchicalLabId(Long id, Long labId);

    // Kept for basic lookups where we might need to find a deleted user
    Optional<StaffUser> findByUsername(String username);
}
