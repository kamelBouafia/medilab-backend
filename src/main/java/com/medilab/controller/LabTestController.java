package com.medilab.controller;

import com.medilab.dto.LabTestDto;
import com.medilab.service.LabTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lab-tests")
@PreAuthorize("hasRole('Manager')")
@RequiredArgsConstructor
public class LabTestController {

    private final LabTestService labTestService;

    @GetMapping
    public ResponseEntity<List<LabTestDto>> getLabTests() {
        return ResponseEntity.ok(labTestService.getLabTests());
    }

    @PostMapping
    public ResponseEntity<LabTestDto> addLabTest(@RequestBody LabTestDto labTestDto) {
        return new ResponseEntity<>(labTestService.addLabTest(labTestDto), HttpStatus.CREATED);
    }

    @PutMapping("/{testId}")
    public ResponseEntity<LabTestDto> updateLabTest(@PathVariable Long testId, @RequestBody LabTestDto labTestDto) {
        return ResponseEntity.ok(labTestService.updateLabTest(testId, labTestDto));
    }

    @DeleteMapping("/{testId}")
    public ResponseEntity<Void> deleteLabTest(@PathVariable Long testId) {
        labTestService.deleteLabTest(testId);
        return ResponseEntity.noContent().build();
    }
}
