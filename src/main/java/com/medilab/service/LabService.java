package com.medilab.service;

import com.medilab.entity.Lab;
import com.medilab.repository.LabRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LabService {
    private final LabRepository labRepository;

    public Lab createLab(Lab lab) {
        return labRepository.save(lab);
    }

    public java.util.List<Lab> findAll() {
        return labRepository.findAll();
    }

    public java.util.Optional<Lab> findById(Long id) {
        return labRepository.findById(id);
    }
}
