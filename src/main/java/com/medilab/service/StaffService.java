package com.medilab.service;

import com.medilab.entity.StaffUser;
import com.medilab.repository.StaffUserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StaffService {
    private final StaffUserRepository repo;
    public StaffService(StaffUserRepository repo) { this.repo = repo; }
    public List<StaffUser> findAllByLab(Long labId){ return repo.findByLabId(labId); }
    public java.util.Optional<StaffUser> findById(Long id){ return repo.findById(id); }
}
