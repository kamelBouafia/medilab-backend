package com.medilab.repository;

import com.medilab.entity.StaffUser;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StaffUserRepository extends JpaRepository<StaffUser, String> {
    List<StaffUser> findAllByLabId(String labId);
}
