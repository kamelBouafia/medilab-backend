package com.medilab.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medilab.dto.SupportContactRequest;
import com.medilab.dto.NotificationRequestDTO;
import com.medilab.entity.Lab;
import com.medilab.entity.StaffUser;
import com.medilab.repository.LabRepository;
import com.medilab.repository.StaffUserRepository;
import com.medilab.repository.SupportTicketRepository;
import com.medilab.security.AuthenticatedUser;
import com.medilab.service.NotificationProducerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(com.medilab.config.TestContainersConfig.class)
public class SupportNotificationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LabRepository labRepository;

    @Autowired
    private StaffUserRepository staffUserRepository;

    @Autowired
    private SupportTicketRepository supportTicketRepository;

    @MockBean
    private NotificationProducerService notificationProducerService;

    private Lab lab;
    private static final String LAB_EMAIL = "kkamel.bbouafia@gmail.com";
    private static final String ADMIN_EMAIL = "ak_bouafia@esi.dz";

    @BeforeEach
    void setUp() {
        supportTicketRepository.deleteAll();
        staffUserRepository.deleteAll();
        labRepository.deleteAll();

        lab = labRepository.save(Lab.builder()
                .name("Notification Test Lab")
                .location("Test Location")
                .contactEmail(LAB_EMAIL)
                .build());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void whenStaffCreatesTicket_thenNotifySystemAdmin() throws Exception {
        // Given
        StaffUser staff = staffUserRepository.save(StaffUser.builder()
                .username("test_staff")
                .password("password")
                .name("Test Staff")
                .email("staff@test.com")
                .role(StaffUser.Role.Manager)
                .lab(lab)
                .build());

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                staff.getId(), lab.getId(), null, staff.getUsername(), staff.getPassword(),
                Collections.emptyList(), "staff", false, true, true, null);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(authenticatedUser, null, authenticatedUser.getAuthorities()));

        SupportContactRequest request = new SupportContactRequest();
        request.setSubject("Staff Issue");
        request.setMessage("Needs admin help");
        request.setName("Name");

        // When
        mockMvc.perform(post("/api/support/contact")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Then
        ArgumentCaptor<NotificationRequestDTO> captor = ArgumentCaptor.forClass(NotificationRequestDTO.class);
        verify(notificationProducerService).sendNotification(captor.capture());
        assertEquals(ADMIN_EMAIL, captor.getValue().getRecipient());
    }

    @Test
    void whenPatientCreatesTicket_thenNotifyLabManager() throws Exception {
        // Given
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                123L, lab.getId(), null, "test_patient", "password",
                Collections.emptyList(), "patient", false, true, true, null);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(authenticatedUser, null, authenticatedUser.getAuthorities()));

        SupportContactRequest request = new SupportContactRequest();
        request.setSubject("Patient Issue");
        request.setMessage("Needs lab help");
        request.setName("Name");

        // When
        mockMvc.perform(post("/api/support/contact")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Then
        ArgumentCaptor<NotificationRequestDTO> captor = ArgumentCaptor.forClass(NotificationRequestDTO.class);
        verify(notificationProducerService).sendNotification(captor.capture());
        assertEquals(LAB_EMAIL, captor.getValue().getRecipient());
    }

    @Test
    void whenAnonymousCreatesTicketWithLabId_thenNotifyLabManager() throws Exception {
        // Given
        SupportContactRequest request = new SupportContactRequest();
        request.setName("Guest User");
        request.setEmail("guest@example.com");
        request.setSubject("Guest Issue");
        request.setMessage("Needs help from this lab");
        request.setLabId(lab.getId());

        // When
        mockMvc.perform(post("/api/support/contact")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Then
        ArgumentCaptor<NotificationRequestDTO> captor = ArgumentCaptor.forClass(NotificationRequestDTO.class);
        verify(notificationProducerService).sendNotification(captor.capture());
        assertEquals(LAB_EMAIL, captor.getValue().getRecipient());
    }
}
