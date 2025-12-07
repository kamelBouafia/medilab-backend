package com.medilab.controller;

import com.medilab.dto.RequisitionDto;
import com.medilab.service.RequisitionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requisitions")
@PreAuthorize("hasRole('Staff')")
@RequiredArgsConstructor
public class RequisitionController {

    private final RequisitionService requisitionService;

    @GetMapping
    public ResponseEntity<List<RequisitionDto>> getRequisitions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "id") String _sort,
            @RequestParam(defaultValue = "desc") String _order,
            @RequestParam MultiValueMap<String, String> params) {
        Page<RequisitionDto> requisitionPage = requisitionService.getRequisitions(page, limit, q, _sort, _order, params);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(requisitionPage.getTotalElements()));
        return ResponseEntity.ok().headers(headers).body(requisitionPage.getContent());
    }

    @PostMapping
    public ResponseEntity<RequisitionDto> createRequisition(@Valid @RequestBody RequisitionDto requisitionDto) {
        RequisitionDto createdRequisition = requisitionService.createRequisition(requisitionDto);
        return new ResponseEntity<>(createdRequisition, HttpStatus.CREATED);
    }

    @PatchMapping("/{requisitionId}/status")
    public ResponseEntity<RequisitionDto> updateRequisitionStatus(@PathVariable Long requisitionId, @Valid @RequestBody RequisitionDto requisitionDto) {
        RequisitionDto updatedRequisition = requisitionService.updateRequisitionStatus(requisitionId, requisitionDto);
        return ResponseEntity.ok(updatedRequisition);
    }
}
