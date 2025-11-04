package com.medilab.service;

import com.medilab.dto.LabTestDto;
import com.medilab.entity.Lab;
import com.medilab.entity.LabTest;
import com.medilab.exception.ResourceNotFoundException;
import com.medilab.mapper.LabTestMapper;
import com.medilab.repository.LabRepository;
import com.medilab.repository.LabTestRepository;
import com.medilab.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LabTestService {

    private final LabTestRepository labTestRepository;
    private final LabTestMapper labTestMapper;
    private final LabRepository labRepository;
    private final AuditLogService auditLogService;

    public List<LabTestDto> getLabTests() {
        AuthenticatedUser user = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return labTestRepository.findByLabId(user.getLabId()).stream()
                .map(labTestMapper::toDto)
                .collect(Collectors.toList());
    }

    public LabTestDto addLabTest(LabTestDto labTestDto) {
        AuthenticatedUser user = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        LabTest labTest = labTestMapper.toEntity(labTestDto);
        Lab lab = labRepository.findById(user.getLabId()).orElseThrow(() -> new ResourceNotFoundException("Lab not found"));
        labTest.setLab(lab);
        LabTest savedLabTest = labTestRepository.save(labTest);
        auditLogService.logAction("LAB_TEST_CREATED", "Lab Test '" + savedLabTest.getName() + "' (ID: " + savedLabTest.getId() + ") was created.");
        return labTestMapper.toDto(savedLabTest);
    }

    public LabTestDto updateLabTest(Long testId, LabTestDto labTestDto) {
        AuthenticatedUser user = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        LabTest existingLabTest = labTestRepository.findByIdAndLabId(testId, user.getLabId()).orElseThrow(() -> new ResourceNotFoundException("LabTest not found"));
        existingLabTest.setName(labTestDto.getName());
        existingLabTest.setCategory(labTestDto.getCategory());
        existingLabTest.setPrice(labTestDto.getPrice());
        LabTest updatedLabTest = labTestRepository.save(existingLabTest);
        auditLogService.logAction("LAB_TEST_UPDATED", "Lab Test '" + updatedLabTest.getName() + "' (ID: " + updatedLabTest.getId() + ") was updated.");
        return labTestMapper.toDto(updatedLabTest);
    }

    public void deleteLabTest(Long testId) {
        AuthenticatedUser user = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        LabTest existingLabTest = labTestRepository.findByIdAndLabId(testId, user.getLabId())
                .orElseThrow(() -> new ResourceNotFoundException("LabTest not found"));
        labTestRepository.deleteById(testId);
        auditLogService.logAction("LAB_TEST_DELETED", "Lab Test '" + existingLabTest.getName() + "' (ID: " + existingLabTest.getId() + ") was deleted.");
    }
}
