package com.medilab.repository;

import com.medilab.entity.StaffUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StaffUserRepository extends JpaRepository<StaffUser, Long> {
    Optional<StaffUser> findByUsername(String username);
    List<StaffUser> findByLabId(Long labId);
}
