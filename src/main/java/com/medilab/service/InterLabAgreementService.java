package com.medilab.service;

import com.medilab.dto.AgreementTestPriceDto;
import com.medilab.dto.CreateAgreementRequestDto;
import com.medilab.dto.InterLabAgreementDto;
import com.medilab.dto.LabDto;
import com.medilab.dto.LabTestDto;
import com.medilab.dto.ReviewAgreementDto;
import com.medilab.entity.*;
import com.medilab.enums.AgreementStatus;
import com.medilab.enums.TestType;
import com.medilab.exception.ResourceNotFoundException;
import com.medilab.mapper.InterLabAgreementMapper;
import com.medilab.mapper.LabTestMapper;
import com.medilab.repository.*;
import com.medilab.security.AuthenticatedUser;
import com.medilab.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterLabAgreementService {

    private final InterLabAgreementRepository agreementRepository;
    private final AgreementTestPriceRepository testPriceRepository;
    private final LabRepository labRepository;
    private final LabTestRepository labTestRepository;
    private final StaffUserRepository staffUserRepository;
    private final InterLabAgreementMapper agreementMapper;
    private final LabTestMapper labTestMapper;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<LabDto> getPotentialPartnerLabs() {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();
        // Return other main labs using the new repository method
        // Note: Assuming Lab entity has getters for fields matching LabDto
        return labRepository.findByIdNotAndParentLabIsNull(user.getLabId()).stream()
                .map(lab -> LabDto.builder()
                        .id(lab.getId())
                        .name(lab.getName())
                        .contactEmail(lab.getContactEmail())
                        .location(lab.getLocation())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LabTestDto> getPartnerLabTests(Long partnerLabId) {
        // Simple security check: verify the lab exists
        if (!labRepository.existsById(partnerLabId)) {
            throw new ResourceNotFoundException("Partner lab not found");
        }
        return labTestRepository.findByLabId(partnerLabId).stream()
                .map(labTestMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LabTestDto> getAvailablePartnerTests(Long partnerLabId) {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();

        // Verify the partner lab exists
        if (!labRepository.existsById(partnerLabId)) {
            throw new ResourceNotFoundException("Partner lab not found");
        }

        // Get all tests from partner lab
        List<LabTest> allTests = labTestRepository.findByLabId(partnerLabId);

        // Get test IDs that already have active agreements
        List<Long> existingTestIds = agreementRepository.findTestIdsWithActiveAgreements(
                user.getLabId(), partnerLabId);

        // Filter out tests with existing agreements
        return allTests.stream()
                .filter(test -> !existingTestIds.contains(test.getId()))
                .map(labTestMapper::toDto)
                .toList();
    }

    @Transactional
    public InterLabAgreementDto createAgreementRequest(CreateAgreementRequestDto dto) {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();

        // Get main lab (current user's lab)
        Lab mainLab = labRepository.findById(user.getLabId())
                .orElseThrow(() -> new ResourceNotFoundException("Main lab not found"));

        // Get partner lab
        Lab partnerLab = labRepository.findById(dto.getPartnerLabId())
                .orElseThrow(() -> new ResourceNotFoundException("Partner lab not found"));

        // Prevent self-agreements
        if (mainLab.getId().equals(partnerLab.getId())) {
            throw new IllegalArgumentException("Cannot create agreement with your own lab");
        }

        // Get requesting staff user
        StaffUser requestedBy = staffUserRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff user not found"));

        // Validate all tests exist in partner lab's catalog
        // AND check for duplicates with existing agreements
        List<Long> existingTestIds = agreementRepository.findTestIdsWithActiveAgreements(
                mainLab.getId(), partnerLab.getId());

        for (AgreementTestPriceDto testPrice : dto.getTestPrices()) {
            LabTest labTest = labTestRepository.findById(testPrice.getTestId())
                    .orElseThrow(() -> new ResourceNotFoundException("Test not found: " + testPrice.getTestId()));

            if (!labTest.getLab().getId().equals(partnerLab.getId())) {
                throw new IllegalArgumentException("Test " + labTest.getName() + " is not available in partner lab");
            }

            // Check if this test already has an active agreement
            if (existingTestIds.contains(labTest.getId())) {
                throw new IllegalArgumentException(
                        "Test " + labTest.getName() + " already has an active agreement with this lab");
            }
        }

        // Create agreement
        InterLabAgreement agreement = InterLabAgreement.builder()
                .mainLab(mainLab)
                .partnerLab(partnerLab)
                .status(AgreementStatus.PENDING)
                .requestedBy(requestedBy)
                .notes(dto.getNotes())
                .validFrom(dto.getValidFrom())
                .validTo(dto.getValidTo())
                .version(1)
                .build();

        InterLabAgreement savedAgreement = agreementRepository.save(agreement);

        // Create test prices
        List<AgreementTestPrice> testPrices = new ArrayList<>();
        for (AgreementTestPriceDto testPriceDto : dto.getTestPrices()) {
            LabTest labTest = labTestRepository.findById(testPriceDto.getTestId())
                    .orElseThrow(() -> new ResourceNotFoundException("Test not found"));

            AgreementTestPrice testPrice = AgreementTestPrice.builder()
                    .agreement(savedAgreement)
                    .labTest(labTest)
                    .interLabPrice(testPriceDto.getInterLabPrice())
                    .priceType(testPriceDto.getPriceType())
                    .discountPercentage(testPriceDto.getDiscountPercentage())
                    .patientPrice(testPriceDto.getPatientPrice())
                    .build();

            testPrices.add(testPrice);
        }

        testPriceRepository.saveAll(testPrices);
        savedAgreement.setTestPrices(testPrices);

        log.info("Agreement request created: ID {} from lab {} to lab {}",
                savedAgreement.getId(), mainLab.getName(), partnerLab.getName());
        auditLogService.logAction("AGREEMENT_REQUESTED",
                "Agreement request created with " + partnerLab.getName() + " for " + testPrices.size() + " tests");

        return agreementMapper.toDto(savedAgreement);
    }

    @Transactional
    public InterLabAgreementDto reviewAgreement(Long agreementId, ReviewAgreementDto dto) {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();

        InterLabAgreement agreement = agreementRepository.findById(agreementId)
                .orElseThrow(() -> new ResourceNotFoundException("Agreement not found"));

        // Validate user is from partner lab
        if (!agreement.getPartnerLab().getId().equals(user.getLabId())) {
            throw new AccessDeniedException("Only partner lab can review this agreement");
        }

        // Validate agreement is in PENDING status
        if (agreement.getStatus() != AgreementStatus.PENDING) {
            throw new IllegalStateException("Agreement is not in PENDING status");
        }

        StaffUser reviewedBy = staffUserRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff user not found"));

        agreement.setReviewedBy(reviewedBy);

        switch (dto.getAction()) {
            case APPROVE:
                agreement.setStatus(AgreementStatus.APPROVED);
                agreement.setValidFrom(dto.getValidFrom() != null ? dto.getValidFrom() : LocalDateTime.now());
                if (dto.getValidTo() != null) {
                    agreement.setValidTo(dto.getValidTo());
                }
                syncOutsourcedTests(agreement);
                log.info("Agreement {} approved", agreementId);
                auditLogService.logAction("AGREEMENT_APPROVED", "Agreement #" + agreementId + " approved");
                break;

            case COUNTER_OFFER:
                agreement.setStatus(AgreementStatus.COUNTER_OFFER);

                // Update test prices if provided
                if (dto.getTestPrices() != null && !dto.getTestPrices().isEmpty()) {
                    // Clear existing prices and flush to ensure they are deleted before adding new
                    // ones
                    // This avoids unique constraint violations: (agreement_id, lab_test_id)
                    agreement.getTestPrices().clear();
                    agreementRepository.flush();

                    for (AgreementTestPriceDto testPriceDto : dto.getTestPrices()) {
                        LabTest labTest = labTestRepository.findById(testPriceDto.getTestId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                        "Test not found: " + testPriceDto.getTestId()));

                        AgreementTestPrice testPrice = AgreementTestPrice.builder()
                                .agreement(agreement)
                                .labTest(labTest)
                                .interLabPrice(testPriceDto.getInterLabPrice())
                                .priceType(testPriceDto.getPriceType())
                                .discountPercentage(testPriceDto.getDiscountPercentage())
                                .patientPrice(testPriceDto.getPatientPrice())
                                .build();

                        agreement.getTestPrices().add(testPrice);
                    }
                }

                if (dto.getValidFrom() != null) {
                    agreement.setValidFrom(dto.getValidFrom());
                }
                if (dto.getValidTo() != null) {
                    agreement.setValidTo(dto.getValidTo());
                }

                log.info("Agreement {} counter-offered", agreementId);
                auditLogService.logAction("AGREEMENT_COUNTER_OFFERED",
                        "Agreement #" + agreementId + " counter-offered");
                break;

            case REJECT:
                agreement.setStatus(AgreementStatus.REJECTED);
                log.info("Agreement {} rejected", agreementId);
                auditLogService.logAction("AGREEMENT_REJECTED", "Agreement #" + agreementId + " rejected");
                break;
        }

        if (dto.getNotes() != null) {
            agreement.setNotes(dto.getNotes());
        }

        agreement.setVersion(agreement.getVersion() + 1);
        InterLabAgreement savedAgreement = agreementRepository.save(agreement);

        return agreementMapper.toDto(savedAgreement);
    }

    @Transactional
    public InterLabAgreementDto confirmCounterOffer(Long agreementId, boolean accept) {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();

        InterLabAgreement agreement = agreementRepository.findById(agreementId)
                .orElseThrow(() -> new ResourceNotFoundException("Agreement not found"));

        // Validate user is from main lab
        if (!agreement.getMainLab().getId().equals(user.getLabId())) {
            throw new AccessDeniedException("Only main lab can confirm counter-offers");
        }

        // Validate agreement is in COUNTER_OFFER status
        if (agreement.getStatus() != AgreementStatus.COUNTER_OFFER) {
            throw new IllegalStateException("Agreement is not in COUNTER_OFFER status");
        }

        if (accept) {
            agreement.setStatus(AgreementStatus.APPROVED);
            if (agreement.getValidFrom() == null) {
                agreement.setValidFrom(LocalDateTime.now());
            }
            syncOutsourcedTests(agreement);
            log.info("Counter-offer for agreement {} accepted", agreementId);
            auditLogService.logAction("COUNTER_OFFER_ACCEPTED",
                    "Counter-offer for agreement #" + agreementId + " accepted");
        } else {
            agreement.setStatus(AgreementStatus.REJECTED);
            log.info("Counter-offer for agreement {} rejected", agreementId);
            auditLogService.logAction("COUNTER_OFFER_REJECTED",
                    "Counter-offer for agreement #" + agreementId + " rejected");
        }

        agreement.setVersion(agreement.getVersion() + 1);
        InterLabAgreement savedAgreement = agreementRepository.save(agreement);

        return agreementMapper.toDto(savedAgreement);
    }

    @Transactional(readOnly = true)
    public Page<InterLabAgreementDto> getAgreements(AgreementStatus status, int page, int limit, String sort,
            String order) {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();

        Sort.Direction direction = Sort.Direction.fromString(order);
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), limit, Sort.by(direction, sort));

        Page<InterLabAgreement> agreements;
        if (status != null) {
            agreements = agreementRepository.findByLabIdAndStatus(user.getLabId(), status, pageable);
        } else {
            agreements = agreementRepository.findByLabId(user.getLabId(), pageable);
        }

        return agreements.map(agreementMapper::toDto);
    }

    @Transactional(readOnly = true)
    public InterLabAgreementDto getAgreementById(Long agreementId) {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();

        InterLabAgreement agreement = agreementRepository.findById(agreementId)
                .orElseThrow(() -> new ResourceNotFoundException("Agreement not found"));

        // Validate user is from either main or partner lab
        if (!agreement.getMainLab().getId().equals(user.getLabId()) &&
                !agreement.getPartnerLab().getId().equals(user.getLabId())) {
            throw new AccessDeniedException("You don't have access to this agreement");
        }

        return agreementMapper.toDto(agreement);
    }

    @Transactional
    public InterLabAgreementDto terminateAgreement(Long agreementId) {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();

        InterLabAgreement agreement = agreementRepository.findById(agreementId)
                .orElseThrow(() -> new ResourceNotFoundException("Agreement not found"));

        // Validate user is from either main or partner lab
        if (!agreement.getMainLab().getId().equals(user.getLabId()) &&
                !agreement.getPartnerLab().getId().equals(user.getLabId())) {
            throw new AccessDeniedException("You don't have access to this agreement");
        }

        agreement.setStatus(AgreementStatus.INACTIVE);
        agreement.setValidTo(LocalDateTime.now());
        agreement.setVersion(agreement.getVersion() + 1);

        InterLabAgreement savedAgreement = agreementRepository.save(agreement);

        log.info("Agreement {} terminated", agreementId);
        auditLogService.logAction("AGREEMENT_TERMINATED", "Agreement #" + agreementId + " terminated");

        return agreementMapper.toDto(savedAgreement);
    }

    @Transactional
    public InterLabAgreementDto updateAgreementPrices(Long agreementId, List<AgreementTestPriceDto> testPrices) {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();

        InterLabAgreement agreement = agreementRepository.findById(agreementId)
                .orElseThrow(() -> new ResourceNotFoundException("Agreement not found"));

        // Validate agreement is APPROVED
        if (agreement.getStatus() != AgreementStatus.APPROVED) {
            throw new IllegalStateException("Can only update prices for approved agreements");
        }

        // Validate user is from either main or partner lab
        if (!agreement.getMainLab().getId().equals(user.getLabId()) &&
                !agreement.getPartnerLab().getId().equals(user.getLabId())) {
            throw new AccessDeniedException("You don't have access to this agreement");
        }

        // Clear existing prices and flush to ensure they are deleted before adding new
        // ones
        // This avoids unique constraint violations: (agreement_id, lab_test_id)
        agreement.getTestPrices().clear();
        agreementRepository.flush();

        for (AgreementTestPriceDto testPriceDto : testPrices) {
            LabTest labTest = labTestRepository.findById(testPriceDto.getTestId())
                    .orElseThrow(() -> new ResourceNotFoundException("Test not found: " + testPriceDto.getTestId()));

            AgreementTestPrice testPrice = AgreementTestPrice.builder()
                    .agreement(agreement)
                    .labTest(labTest)
                    .interLabPrice(testPriceDto.getInterLabPrice())
                    .priceType(testPriceDto.getPriceType())
                    .discountPercentage(testPriceDto.getDiscountPercentage())
                    .patientPrice(testPriceDto.getPatientPrice())
                    .build();

            agreement.getTestPrices().add(testPrice);
        }
        agreement.setVersion(agreement.getVersion() + 1);

        InterLabAgreement savedAgreement = agreementRepository.save(agreement);

        log.info("Agreement {} prices updated", agreementId);
        auditLogService.logAction("AGREEMENT_PRICES_UPDATED", "Agreement #" + agreementId + " prices updated");

        return agreementMapper.toDto(savedAgreement);
    }

    @Transactional
    public InterLabAgreementDto cancelAgreementRequest(Long agreementId) {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();

        InterLabAgreement agreement = agreementRepository.findById(agreementId)
                .orElseThrow(() -> new ResourceNotFoundException("Agreement not found"));

        // Only main lab (requester) can cancel
        if (!agreement.getMainLab().getId().equals(user.getLabId())) {
            throw new AccessDeniedException("Only the requesting lab can cancel this agreement");
        }

        // Can only cancel if PENDING, COUNTER_OFFER or APPROVED
        if (agreement.getStatus() != AgreementStatus.PENDING
                && agreement.getStatus() != AgreementStatus.COUNTER_OFFER
                && agreement.getStatus() != AgreementStatus.APPROVED) {
            throw new IllegalStateException(
                    "Agreement cannot be cancelled in its current state: " + agreement.getStatus());
        }

        agreement.setStatus(AgreementStatus.CANCELLED);
        agreement.setVersion(agreement.getVersion() + 1);

        InterLabAgreement savedAgreement = agreementRepository.save(agreement);

        log.info("Agreement {} cancelled by requester", agreementId);
        auditLogService.logAction("AGREEMENT_CANCELLED", "Agreement #" + agreementId + " cancelled by requester");

        return agreementMapper.toDto(savedAgreement);
    }

    @Transactional
    public InterLabAgreementDto updateAgreementRequest(Long agreementId, CreateAgreementRequestDto dto) {
        AuthenticatedUser user = SecurityUtils.getAuthenticatedUser();

        InterLabAgreement agreement = agreementRepository.findById(agreementId)
                .orElseThrow(() -> new ResourceNotFoundException("Agreement not found"));

        // Only main lab (requester) can update
        if (!agreement.getMainLab().getId().equals(user.getLabId())) {
            throw new AccessDeniedException("Only the requesting lab can update this agreement");
        }

        // Can only update if PENDING, COUNTER_OFFER, APPROVED, CANCELLED or REJECTED
        if (agreement.getStatus() != AgreementStatus.PENDING
                && agreement.getStatus() != AgreementStatus.COUNTER_OFFER
                && agreement.getStatus() != AgreementStatus.APPROVED
                && agreement.getStatus() != AgreementStatus.CANCELLED
                && agreement.getStatus() != AgreementStatus.REJECTED) {
            throw new IllegalStateException(
                    "Agreement cannot be updated in its current state: " + agreement.getStatus());
        }

        // Update fields
        agreement.setNotes(dto.getNotes());
        agreement.setValidFrom(dto.getValidFrom());
        agreement.setValidTo(dto.getValidTo());
        agreement.setStatus(AgreementStatus.PENDING); // Reset to PENDING if it was COUNTER_OFFER, APPROVED, CANCELLED
                                                      // or REJECTED

        // Update test prices
        agreement.getTestPrices().clear();
        agreementRepository.flush();

        for (AgreementTestPriceDto testPriceDto : dto.getTestPrices()) {
            LabTest labTest = labTestRepository.findById(testPriceDto.getTestId())
                    .orElseThrow(() -> new ResourceNotFoundException("Test not found: " + testPriceDto.getTestId()));

            AgreementTestPrice testPrice = AgreementTestPrice.builder()
                    .agreement(agreement)
                    .labTest(labTest)
                    .interLabPrice(testPriceDto.getInterLabPrice())
                    .priceType(testPriceDto.getPriceType())
                    .discountPercentage(testPriceDto.getDiscountPercentage())
                    .patientPrice(testPriceDto.getPatientPrice())
                    .build();

            agreement.getTestPrices().add(testPrice);
        }

        agreement.setVersion(agreement.getVersion() + 1);
        InterLabAgreement savedAgreement = agreementRepository.save(agreement);

        log.info("Agreement {} updated by requester", agreementId);
        auditLogService.logAction("AGREEMENT_UPDATED", "Agreement #" + agreementId + " updated by requester");

        return agreementMapper.toDto(savedAgreement);
    }

    private void syncOutsourcedTests(InterLabAgreement agreement) {
        Lab requestingLab = agreement.getMainLab();
        log.info("Starting sync of outsourced tests for agreement #{} to lab {}", agreement.getId(),
                requestingLab.getName());

        for (AgreementTestPrice negotiatedPrice : agreement.getTestPrices()) {
            LabTest partnerTest = negotiatedPrice.getLabTest();
            GlobalTestCatalog globalTest = partnerTest.getGlobalTest();
            String targetCode = (partnerTest.getCode() != null ? partnerTest.getCode()
                    : partnerTest.getName().replaceAll("\\s+", "_")) + "-OUT";

            java.util.Optional<LabTest> existingOutsourced = java.util.Optional.empty();

            if (globalTest != null) {
                existingOutsourced = labTestRepository.findByLabIdAndGlobalTestIdAndType(
                        requestingLab.getId(), globalTest.getId(), TestType.OUTSOURCED);
            }

            // Fallback to code if not found by global test or if global test is null
            if (existingOutsourced.isEmpty()) {
                existingOutsourced = labTestRepository.findByLabIdAndCodeAndType(
                        requestingLab.getId(), targetCode, TestType.OUTSOURCED);
            }

            BigDecimal finalPrice;
            if (negotiatedPrice.getPriceType() == com.medilab.enums.PriceType.FIXED) {
                finalPrice = negotiatedPrice.getInterLabPrice();
            } else {
                // Percentage discount on partner's current base price
                BigDecimal partnerPrice = partnerTest.getPrice();
                BigDecimal discount = negotiatedPrice.getDiscountPercentage()
                        .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
                finalPrice = partnerPrice.multiply(BigDecimal.ONE.subtract(discount))
                        .setScale(2, RoundingMode.HALF_UP);
            }

            BigDecimal catalogPrice = negotiatedPrice.getPatientPrice() != null
                    ? negotiatedPrice.getPatientPrice()
                    : finalPrice;

            if (existingOutsourced.isPresent()) {
                LabTest testToUpdate = existingOutsourced.get();
                testToUpdate.setPrice(catalogPrice);
                testToUpdate.setPartnerLab(agreement.getPartnerLab());
                // Also update metadata just in case
                testToUpdate.setName(partnerTest.getName());
                testToUpdate.setCategory(partnerTest.getCategory());
                testToUpdate.setUnit(partnerTest.getUnit());
                testToUpdate.setMinVal(partnerTest.getMinVal());
                testToUpdate.setMaxVal(partnerTest.getMaxVal());

                labTestRepository.save(testToUpdate);
                log.info("Updated existing OUTSOURCED test {} (ID: {}) for lab {}", testToUpdate.getName(),
                        testToUpdate.getId(), requestingLab.getName());
            } else {
                // Create new OUTSOURCED test
                LabTest newTest = LabTest.builder()
                        .name(partnerTest.getName())
                        .code(targetCode)
                        .category(partnerTest.getCategory())
                        .unit(partnerTest.getUnit())
                        .minVal(partnerTest.getMinVal())
                        .maxVal(partnerTest.getMaxVal())
                        .criticalMinVal(partnerTest.getCriticalMinVal())
                        .criticalMaxVal(partnerTest.getCriticalMaxVal())
                        .description("Outsourced from " + agreement.getPartnerLab().getName() + ". "
                                + (partnerTest.getDescription() != null ? partnerTest.getDescription() : ""))
                        .price(catalogPrice)
                        .type(TestType.OUTSOURCED)
                        .lab(requestingLab)
                        .partnerLab(agreement.getPartnerLab())
                        .globalTest(globalTest)
                        .build();

                LabTest saved = labTestRepository.save(newTest);
                log.info("Created new OUTSOURCED test {} (ID: {}) for lab {}", saved.getName(), saved.getId(),
                        requestingLab.getName());
            }
        }
    }
}
