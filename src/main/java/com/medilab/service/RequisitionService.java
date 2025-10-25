package com.medilab.service;

import com.medilab.entity.Requisition;
import com.medilab.entity.RequisitionTest;
import com.medilab.repository.RequisitionRepository;
import com.medilab.repository.RequisitionTestRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class RequisitionService {
    private final RequisitionRepository reqRepo;
    private final RequisitionTestRepository rtRepo;
    private final AuditService auditService;

    public RequisitionService(RequisitionRepository reqRepo, RequisitionTestRepository rtRepo, AuditService auditService) {
        this.reqRepo = reqRepo;
        this.rtRepo = rtRepo;
        this.auditService = auditService;
    }

    public Requisition create(String id, String patientId, String doctorName, String createdById, String labId, List<String> tests) {
        Requisition r = Requisition.builder()
                .id(id)
                .patientId(patientId)
                .doctorName(doctorName)
                .date(OffsetDateTime.now())
                .status("Collected")
                .createdById(createdById)
                .labId(labId)
                .build();
        reqRepo.save(r);
        for (String t : tests) {
            RequisitionTest rt = RequisitionTest.builder().requisitionId(id).testId(t).labId(labId).build();
            rtRepo.save(rt);
        }
        auditService.log(createdById, "Create Requisition", "Requisition "+id+" for patient " + patientId, labId);
        return r;
    }

    public List<Requisition> findAllByLab(String labId){ return reqRepo.findAllByLabId(labId); }
}
