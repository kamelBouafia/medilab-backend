package com.medilab.controller;

import com.medilab.dto.LabTestDto;
import com.medilab.service.LabTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
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
    public ResponseEntity<List<LabTestDto>> getLabTests(
            @RequestParam(defaultValue = "0") int _page,
            @RequestParam(defaultValue = "10") int _limit,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "name") String _sort,
            @RequestParam(defaultValue = "asc") String _order) {
        Page<LabTestDto> labTestPage = labTestService.getLabTests(_page, _limit, q, _sort, _order);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(labTestPage.getTotalElements()));
        return ResponseEntity.ok().headers(headers).body(labTestPage.getContent());
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
