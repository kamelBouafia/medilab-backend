package com.medilab.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medilab.dto.LabTestDto;
import com.medilab.entity.Lab;
import com.medilab.entity.StaffUser;
import com.medilab.repository.LabRepository;
import com.medilab.repository.LabTestRepository;
import com.medilab.repository.StaffUserRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(com.medilab.config.TestContainersConfig.class)
public class LabTestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LabRepository labRepository;

    @Autowired
    private StaffUserRepository staffUserRepository;

    @Autowired
    private LabTestRepository labTestRepository;

    private Lab lab;
    private StaffUser manager;
    private StaffUser technician;

    @BeforeEach
    void setUp() {
        tearDown();
        lab = labRepository.save(Lab.builder().name("Test Lab").build());
        manager = staffUserRepository.save(StaffUser.builder()
                .username("manager")
                .password("password")
                .role(StaffUser.Role.Manager)
                .lab(lab)
                .enabled(true)
                .build());
        technician = staffUserRepository.save(StaffUser.builder()
                .username("technician")
                .password("password")
                .role(StaffUser.Role.Technician)
                .lab(lab)
                .enabled(true)
                .build());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        labTestRepository.deleteAll();
        staffUserRepository.deleteAll();
        labRepository.deleteAll();
    }

    private void authenticate(StaffUser user) {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(user.getId(),
                user.getLab().getId(),
                user.getUsername(), user.getPassword(), user.getAuthorities(), "staff",
                false, false, true);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(authenticatedUser, null,
                        authenticatedUser.getAuthorities()));
    }

    @Test
    void technician_can_view_tests() throws Exception {
        authenticate(technician);
        mockMvc.perform(get("/api/lab-tests"))
                .andExpect(status().isOk());
    }

    @Test
    void manager_can_view_tests() throws Exception {
        authenticate(manager);
        mockMvc.perform(get("/api/lab-tests"))
                .andExpect(status().isOk());
    }

    @Test
    void technician_cannot_add_test() throws Exception {
        authenticate(technician);
        LabTestDto dto = LabTestDto.builder()
                .name("New Test")
                .category(com.medilab.enums.TestCategory.HEMATOLOGY)
                .price(java.math.BigDecimal.TEN)
                .build();

        mockMvc.perform(post("/api/lab-tests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void manager_can_add_test() throws Exception {
        authenticate(manager);
        LabTestDto dto = LabTestDto.builder()
                .name("New Test")
                .category(com.medilab.enums.TestCategory.HEMATOLOGY)
                .price(java.math.BigDecimal.TEN)
                .build();

        mockMvc.perform(post("/api/lab-tests")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }
}
