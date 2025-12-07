package com.medilab.service;

import com.medilab.dto.RequisitionDto;
import com.medilab.entity.Requisition;
import com.medilab.entity.SampleStatus;
import com.medilab.exception.ResourceNotFoundException;
import com.medilab.mapper.RequisitionMapper;
import com.medilab.repository.*;
import com.medilab.security.AuthenticatedUser;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RequisitionService {

    private final RequisitionRepository requisitionRepository;
    private final PatientRepository patientRepository;
    private final StaffUserRepository staffUserRepository;
    private final LabRepository labRepository;
    private final LabTestRepository labTestRepository;
    private final RequisitionMapper requisitionMapper;

    @Transactional(readOnly = true)
    public Page<RequisitionDto> getRequisitions(int page, int limit, String q, String sort, String order, MultiValueMap<String, String> params) {
        AuthenticatedUser user = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Sort.Direction direction = Sort.Direction.fromString(order);
        Pageable pageable = PageRequest.of(page > 0 ? page - 1 : 0, limit, Sort.by(direction, sort));

        if (params.containsKey("patientId")) {
            List<Long> patientIds = params.get("patientId").stream().map(Long::valueOf).toList();
            if (patientIds.size() == 1) {
                return requisitionRepository.findByPatientIdAndLabIdWithTestsAndResults(patientIds.get(0), user.getLabId(), pageable)
                        .map(requisitionMapper::toDto);
            }
        }

        Specification<Requisition> spec = (root, query, cb) -> {
            if (query.getResultType() != Long.class) {
                root.fetch("patient");
                root.fetch("tests", JoinType.LEFT);
                root.fetch("testResults", JoinType.LEFT);
            }

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("lab").get("id"), user.getLabId()));

            if (StringUtils.hasText(q)) {
                predicates.add(cb.like(cb.lower(root.get("patient").get("name")), "%" + q.toLowerCase() + "%"));
            }

            params.forEach((key, values) -> {
                switch (key) {
                    case "status_ne":
                        for (String value : values) {
                            try {
                                predicates.add(cb.notEqual(root.get("status"), SampleStatus.valueOf(value)));
                            } catch (IllegalArgumentException e) {
                                // Ignore invalid status values
                            }
                        }
                        break;
                    case "id":
                        if (values.getFirst() != null) {
                            predicates.add(root.get("id").in(values.stream().map(Long::valueOf).toList()));
                        }
                        break;
                    case "patientId":
                        if (values.getFirst() != null) {
                            predicates.add(root.get("patient").get("id").in(values.stream().map(Long::valueOf).toList()));
                        }
                        break;
                    case "createdById":
                        if (values.getFirst() != null) {
                            predicates.add(root.get("createdBy").get("id").in(values.stream().map(Long::valueOf).toList()));
                        }
                        break;
                    case "status":
                        if (values.getFirst() != null) {
                            predicates.add(root.get("status").in(values.stream().map(SampleStatus::valueOf).toList()));
                        }
                        break;
                }
            });

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return requisitionRepository.findAll(spec, pageable).map(requisitionMapper::toDto);
    }

    public RequisitionDto getRequisitionById(Long id) {
        AuthenticatedUser user = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Requisition> requisitionOptional = requisitionRepository.findByIdAndLabIdWithTestsAndResults(id, user.getLabId());
        Requisition requisition = requisitionOptional.orElseThrow(() -> new ResourceNotFoundException("Requisition not found with id: " + id));
        return requisitionMapper.toDto(requisition);
    }

    public RequisitionDto createRequisition(RequisitionDto requisitionDto) {
        AuthenticatedUser user = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Requisition requisition = requisitionMapper.toEntity(requisitionDto);

        patientRepository.findById(requisitionDto.getPatientId()).ifPresent(requisition::setPatient);
        labRepository.findById(user.getLabId()).ifPresent(requisition::setLab);
        staffUserRepository.findById(user.getId()).ifPresent(requisition::setCreatedBy);

        if (requisitionDto.getTestIds() != null) {
            requisition.setTests(new HashSet<>(labTestRepository.findAllById(requisitionDto.getTestIds())));
        }

        Requisition savedRequisition = requisitionRepository.save(requisition);
        return requisitionMapper.toDto(savedRequisition);
    }

    @Transactional
    public RequisitionDto updateRequisitionStatus(Long requisitionId, RequisitionDto requisitionDto) {
        Requisition requisition = requisitionRepository.findById(requisitionId)
                .orElseThrow(() -> new ResourceNotFoundException("Requisition not found with id: " + requisitionId));

        SampleStatus newStatus = SampleStatus.valueOf(requisitionDto.getStatus());

        if (requisition.getStatus() == SampleStatus.COMPLETED || requisition.getStatus() == SampleStatus.CANCELLED) {
            throw new IllegalStateException("Cannot change status of a completed or cancelled requisition.");
        }

        requisition.setStatus(newStatus);

        if (newStatus == SampleStatus.COMPLETED) {
            requisition.setCompletionDate(LocalDateTime.now());
        }

        Requisition updatedRequisition = requisitionRepository.save(requisition);
        return requisitionMapper.toDto(updatedRequisition);
    }
}
