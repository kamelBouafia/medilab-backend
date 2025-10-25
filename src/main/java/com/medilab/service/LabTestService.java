package com.medilab.service;

import com.medilab.dto.LabTestDto;
import com.medilab.mapper.LabTestMapper;
import com.medilab.repository.LabTestRepository;
import com.medilab.security.AuthenticatedUser;
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
        AuthenticatedUser user = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return labTestRepository.findByLabId(user.getLabId()).stream()
                .map(labTestMapper::toDto)
                .collect(Collectors.toList());
    }
}
