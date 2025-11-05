package com.medilab.repository;

import com.medilab.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {
    @Query("SELECT al FROM AuditLog al JOIN FETCH al.user WHERE al.lab.id = :labId ORDER BY al.timestamp DESC")
    List<AuditLog> findByLabIdOrderByTimestampDesc(Long labId);
}
