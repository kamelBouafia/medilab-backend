package com.medilab.repository;

import com.medilab.entity.SupportTicket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    @Query("SELECT new com.medilab.entity.SupportTicket(t.id, t.ticketId, t.name, t.email, t.subject, t.labId, t.userId, t.status, t.createdAt) FROM SupportTicket t WHERE " +
            "(:labId IS NULL OR t.labId = :labId) AND " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:userId IS NULL OR t.userId = :userId) AND " +
            "(:q IS NULL OR (LOWER(t.ticketId) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(t.name) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(t.email) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(t.subject) LIKE LOWER(CONCAT('%',:q,'%'))))")
    Page<SupportTicket> search(@Param("q") String q,
                               @Param("labId") Long labId,
                               @Param("status") String status,
                               @Param("userId") Long userId,
                               Pageable pageable);
}
