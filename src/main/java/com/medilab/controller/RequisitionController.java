package com.medilab.controller;

import com.medilab.dto.RequisitionDto;
import com.medilab.service.RequisitionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/requisitions")
@PreAuthorize("hasRole('Staff')")
@RequiredArgsConstructor
public class RequisitionController {

    private final RequisitionService requisitionService;

    @GetMapping
    public ResponseEntity<Page<RequisitionDto>> getRequisitions(
            @RequestParam(defaultValue = "0") int _page,
            @RequestParam(defaultValue = "10") int _limit,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "id") String _sort,
            @RequestParam(defaultValue = "desc") String _order,
            @RequestParam MultiValueMap<String, String> params) {
        Page<RequisitionDto> requisitionPage = requisitionService.getRequisitions(_page, _limit, q, _sort, _order,
                params);
        return ResponseEntity.ok(requisitionPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RequisitionDto> getRequisitionById(@PathVariable Long id) {
        RequisitionDto requisitionDto = requisitionService.getRequisitionById(id);
        return ResponseEntity.ok(requisitionDto);
    }

    @PostMapping
    public ResponseEntity<RequisitionDto> createRequisition(@Valid @RequestBody RequisitionDto requisitionDto) {
        RequisitionDto createdRequisition = requisitionService.createRequisition(requisitionDto);
        return new ResponseEntity<>(createdRequisition, HttpStatus.CREATED);
    }

    @PatchMapping("/{requisitionId}/status")
    public ResponseEntity<RequisitionDto> updateRequisitionStatus(@PathVariable Long requisitionId,
            @Valid @RequestBody RequisitionDto requisitionDto) {
        RequisitionDto updatedRequisition = requisitionService.updateRequisitionStatus(requisitionId, requisitionDto);
        return ResponseEntity.ok(updatedRequisition);
    }

    @GetMapping("/{id}/report")
    public ResponseEntity<java.util.Map<String, String>> getReportUrl(@PathVariable Long id) {
        String pdfUrl = requisitionService.getReportUrl(id);
        return ResponseEntity.ok(java.util.Collections.singletonMap("url", pdfUrl));
    }

    @PostMapping("/{id}/resend-report")
    public ResponseEntity<Void> resendReport(@PathVariable Long id) {
        requisitionService.resendReport(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/results")
    public ResponseEntity<java.util.List<com.medilab.dto.TestResultDto>> getRequisitionResults(@PathVariable Long id) {
        java.util.List<com.medilab.dto.TestResultDto> results = requisitionService.getRequisitionResults(id);
        return ResponseEntity.ok(results);
    }
}
