package com.medilab.controller;

import com.medilab.dto.AgreementTestPriceDto;
import com.medilab.dto.CreateAgreementRequestDto;
import com.medilab.dto.InterLabAgreementDto;
import com.medilab.dto.LabDto;
import com.medilab.dto.LabTestDto;
import com.medilab.dto.ReviewAgreementDto;
import com.medilab.enums.AgreementStatus;
import com.medilab.service.InterLabAgreementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inter-lab-agreements")
@RequiredArgsConstructor
public class InterLabAgreementController {

    private final InterLabAgreementService agreementService;

    @PostMapping
    @PreAuthorize("hasRole('Manager')")
    public ResponseEntity<InterLabAgreementDto> createAgreement(@Valid @RequestBody CreateAgreementRequestDto dto) {
        return new ResponseEntity<>(agreementService.createAgreementRequest(dto), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('Manager', 'Technician')")
    public ResponseEntity<Page<InterLabAgreementDto>> getAgreements(
            @RequestParam(required = false) AgreementStatus status,
            @RequestParam(defaultValue = "1") int _page,
            @RequestParam(defaultValue = "10") int _limit,
            @RequestParam(defaultValue = "createdAt") String _sort,
            @RequestParam(defaultValue = "desc") String _order) {
        return ResponseEntity.ok(agreementService.getAgreements(status, _page, _limit, _sort, _order));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('Manager', 'Technician')")
    public ResponseEntity<InterLabAgreementDto> getAgreementById(@PathVariable Long id) {
        return ResponseEntity.ok(agreementService.getAgreementById(id));
    }

    @PutMapping("/{id}/review")
    @PreAuthorize("hasRole('Manager')")
    public ResponseEntity<InterLabAgreementDto> reviewAgreement(
            @PathVariable Long id,
            @Valid @RequestBody ReviewAgreementDto dto) {
        return ResponseEntity.ok(agreementService.reviewAgreement(id, dto));
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasRole('Manager')")
    public ResponseEntity<InterLabAgreementDto> confirmCounterOffer(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> request) {
        boolean accept = request.getOrDefault("accept", false);
        return ResponseEntity.ok(agreementService.confirmCounterOffer(id, accept));
    }

    @PutMapping("/{id}/terminate")
    @PreAuthorize("hasRole('Manager')")
    public ResponseEntity<InterLabAgreementDto> terminateAgreement(@PathVariable Long id) {
        return ResponseEntity.ok(agreementService.terminateAgreement(id));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('Manager')")
    public ResponseEntity<InterLabAgreementDto> cancelAgreement(@PathVariable Long id) {
        return ResponseEntity.ok(agreementService.cancelAgreementRequest(id));
    }

    @PutMapping("/{id}/update")
    @PreAuthorize("hasRole('Manager')")
    public ResponseEntity<InterLabAgreementDto> updateAgreement(
            @PathVariable Long id,
            @Valid @RequestBody CreateAgreementRequestDto dto) {
        return ResponseEntity.ok(agreementService.updateAgreementRequest(id, dto));
    }

    @PutMapping("/{id}/prices")
    @PreAuthorize("hasRole('Manager')")
    public ResponseEntity<InterLabAgreementDto> updatePrices(
            @PathVariable Long id,
            @Valid @RequestBody List<AgreementTestPriceDto> testPrices) {
        return ResponseEntity.ok(agreementService.updateAgreementPrices(id, testPrices));
    }

    @GetMapping("/potential-partners")
    @PreAuthorize("hasRole('Manager')")
    public ResponseEntity<List<LabDto>> getPotentialPartners() {
        return ResponseEntity.ok(agreementService.getPotentialPartnerLabs());
    }

    @GetMapping("/partner-tests/{labId}")
    @PreAuthorize("hasRole('Manager')")
    public ResponseEntity<List<LabTestDto>> getPartnerTests(@PathVariable Long labId) {
        return ResponseEntity.ok(agreementService.getAvailablePartnerTests(labId));
    }
}
