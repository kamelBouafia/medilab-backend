package com.medilab.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medilab.dto.RequisitionDto;
import com.medilab.entity.*;
import com.medilab.repository.*;
import com.medilab.security.AuthenticatedUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class RequisitionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RequisitionRepository requisitionRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private StaffUserRepository staffUserRepository;

    @Autowired
    private LabRepository labRepository;

    @Autowired
    private LabTestRepository labTestRepository;

    private Lab lab;
    private StaffUser staffUser1;
    private Patient patient1;
    private Requisition requisition1;

    @BeforeEach
    void setUp() {
        lab = labRepository.save(Lab.builder().name("Test Lab").location("Test Location").build());
        staffUser1 = staffUserRepository.save(StaffUser.builder().username("staff1").password("password").name("Staff One").email("staff1@medilab.com").phone("1112223333").role(StaffUser.Role.Manager).lab(lab).build());
        StaffUser staffUser2 = staffUserRepository.save(StaffUser.builder().username("staff2").password("password").name("Staff Two").email("staff2@medilab.com").phone("4445556666").role(StaffUser.Role.Technician).lab(lab).build());

        // Set up the security context
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(staffUser1.getId(), staffUser1.getLab().getId(), staffUser1.getUsername(), staffUser1.getPassword(), Collections.emptyList(), "staff");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(authenticatedUser, null, authenticatedUser.getAuthorities()));

        patient1 = patientRepository.save(Patient.builder().username("patient1").name("John Doe").phone("1234567890").email("john.doe@example.com").dob(LocalDate.now()).gender(Patient.Gender.Male).address("123 Main St").bloodGroup("A+").allergies("None").createdBy(staffUser1).lab(lab).build());
        Patient patient2 = patientRepository.save(Patient.builder().username("patient2").name("Jane Smith").phone("0987654321").email("jane.smith@example.com").dob(LocalDate.now()).gender(Patient.Gender.Female).address("456 Oak Ave").bloodGroup("B-").allergies("Peanuts").createdBy(staffUser2).lab(lab).build());

        LabTest test1 = labTestRepository.save(LabTest.builder().name("Blood Test").category("Hematology").price(50.0).lab(lab).build());
        LabTest test2 = labTestRepository.save(LabTest.builder().name("Urine Test").category("Urology").price(40.0).lab(lab).build());

        requisition1 = Requisition.builder()
                .patient(patient1)
                .doctorName("Dr. Strange")
                .status(SampleStatus.PROCESSING)
                .createdBy(staffUser1)
                .lab(lab)
                .tests(new HashSet<>(Arrays.asList(test1, test2)))
                .build();

        Requisition req2 = Requisition.builder()
                .patient(patient2)
                .doctorName("Dr. Who")
                .status(SampleStatus.COLLECTED)
                .createdBy(staffUser2)
                .lab(lab)
                .tests(new HashSet<>(Arrays.asList(test1)))
                .build();

        requisitionRepository.saveAll(Arrays.asList(requisition1, req2));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        requisitionRepository.deleteAll();
        labTestRepository.deleteAll();
        patientRepository.deleteAll();
        staffUserRepository.deleteAll();
        labRepository.deleteAll();
    }

    @Test
    void getRequisitions_shouldReturnAllRequisitions() throws Exception {
        mockMvc.perform(get("/api/requisitions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getRequisitions_shouldFilterById() throws Exception {
        mockMvc.perform(get("/api/requisitions").param("id", requisition1.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(requisition1.getId()));
    }

    @Test
    void getRequisitions_shouldFilterByPatientId() throws Exception {
        mockMvc.perform(get("/api/requisitions").param("patientId", patient1.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].patientId").value(patient1.getId()));
    }

    @Test
    void getRequisitions_shouldFilterByCreatedById() throws Exception {
        mockMvc.perform(get("/api/requisitions").param("createdById", staffUser1.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].createdById").value(staffUser1.getId()));
    }

    @Test
    void getRequisitions_shouldFilterByStatus() throws Exception {
        mockMvc.perform(get("/api/requisitions").param("status", "PROCESSING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("PROCESSING"));
    }

    @Test
    void createRequisition_shouldCreateRequisition() throws Exception {
        LabTest test = labTestRepository.save(LabTest.builder().name("X-Ray").category("Radiology").price(150.0).lab(lab).build());
        RequisitionDto newRequisitionDto = RequisitionDto.builder()
                .patientId(patient1.getId())
                .doctorName("Dr. Doom")
                .testIds(new HashSet<>(Arrays.asList(test.getId())))
                .build();

        mockMvc.perform(post("/api/requisitions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newRequisitionDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.doctorName").value("Dr. Doom"));
    }

    @Test
    void updateRequisitionStatus_shouldUpdateStatus() throws Exception {
        RequisitionDto statusUpdateDto = new RequisitionDto();
        statusUpdateDto.setStatus("COLLECTED");

        mockMvc.perform(patch("/api/requisitions/{requisitionId}/status", requisition1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COLLECTED"));
    }
}
