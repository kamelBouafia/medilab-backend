package com.medilab.repository;

import com.medilab.entity.Lab;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabRepository extends JpaRepository<Lab, String> { }
