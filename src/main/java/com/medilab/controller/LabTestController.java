package com.medilab.controller;

import com.medilab.dto.LabTestDto;
import com.medilab.service.LabTestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lab-tests")
@PreAuthorize("hasRole('Manager')")
@RequiredArgsConstructor
public class LabTestController {

    private final LabTestService labTestService;

    @GetMapping
    public ResponseEntity<Page<LabTestDto>> getLabTests(
            @RequestParam(defaultValue = "1") int _page,
            @RequestParam(defaultValue = "10") int _limit,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "name") String _sort,
            @RequestParam(defaultValue = "asc") String _order) {
        Page<LabTestDto> labTestPage = labTestService.getLabTests(_page, _limit, q, _sort, _order);
        return ResponseEntity.ok(labTestPage);
    }

    @PostMapping
    public ResponseEntity<LabTestDto> addLabTest(@Valid @RequestBody LabTestDto labTestDto) {
        return new ResponseEntity<>(labTestService.addLabTest(labTestDto), HttpStatus.CREATED);
    }

    @PostMapping("/import")
    public ResponseEntity<LabTestDto> importTest(
            @RequestParam Long globalTestId,
            @RequestParam java.math.BigDecimal price) {
        return new ResponseEntity<>(labTestService.importTestFromGlobal(globalTestId, price), HttpStatus.CREATED);
    }

    @PostMapping("/import-bulk")
    public ResponseEntity<java.util.List<LabTestDto>> importTests(@RequestBody com.medilab.dto.BulkImportDto request) {
        return new ResponseEntity<>(labTestService.importTestsFromGlobal(request.getItems()), HttpStatus.CREATED);
    }

    @PutMapping("/{testId}")
    public ResponseEntity<LabTestDto> updateLabTest(@PathVariable Long testId,
            @Valid @RequestBody LabTestDto labTestDto) {
        return ResponseEntity.ok(labTestService.updateLabTest(testId, labTestDto));
    }

    @DeleteMapping("/{testId}")
    public ResponseEntity<Void> deleteLabTest(@PathVariable Long testId) {
        labTestService.deleteLabTest(testId);
        return ResponseEntity.noContent().build();
    }
}
