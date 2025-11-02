package com.medilab.service;

import com.medilab.dto.LabTestDto;
import com.medilab.entity.StaffUser;
import com.medilab.mapper.LabTestMapper;
import com.medilab.repository.LabTestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LabTestService {

    @Autowired
    private LabTestRepository labTestRepository;

    @Autowired
    private LabTestMapper labTestMapper;

    public List<LabTestDto> getLabTests() {
        StaffUser user = (StaffUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return labTestRepository.findByLabId(user.getLab().getId()).stream()
                .map(labTestMapper::toDto)
                .collect(Collectors.toList());
    }
}
