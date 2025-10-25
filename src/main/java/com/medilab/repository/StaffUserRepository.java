package com.medilab.repository;

import com.medilab.entity.StaffUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffUserRepository extends JpaRepository<StaffUser, Long> {
    List<StaffUser> findByLabId(Long labId);
}
