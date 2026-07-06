package com.wise.petadoption.adoption.controller;

import com.wise.petadoption.adoption.api.ApplicationResponse;
import com.wise.petadoption.adoption.common.ApplicationStatus;
import com.wise.petadoption.adoption.domain.CreateApplicationCommand;
import com.wise.petadoption.adoption.domain.RejectionCommand;
import com.wise.petadoption.adoption.mapper.ApplicationResponseMapper;
import com.wise.petadoption.adoption.service.ApplicationService;
import com.wise.petadoption.security.domain.SecurityUser;
import com.wise.petadoption.support.TestFixtures;
import com.wise.petadoption.support.WebMvcTestSecurityConfig;
import com.wise.petadoption.user.common.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static com.wise.petadoption.support.TestFixtures.application;
import static com.wise.petadoption.support.TestFixtures.securityUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApplicationController.class)
@Import(WebMvcTestSecurityConfig.class)
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApplicationService applicationService;
    @MockitoBean
    private ApplicationResponseMapper responseMapper;

    private ApplicationResponse applicationResponse() {
        return new ApplicationResponse(10L, 5L, 2L, "Please", ApplicationStatus.PENDING,
                null, TestFixtures.NOW, TestFixtures.NOW);
    }

    private RequestPostProcessor admin() {
        SecurityUser principal = securityUser(1L, "admin@example.com", Role.ROLE_ADMIN);
        return authentication(new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()));
    }

    private RequestPostProcessor applicant() {
        SecurityUser principal = securityUser(2L, "user@example.com", Role.ROLE_USER);
        return authentication(new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()));
    }

    @Test
    void create_UserRole_ReturnsCreatedWithLocation() throws Exception {
        when(applicationService.create(any(CreateApplicationCommand.class)))
                .thenReturn(application(10L, 5L, 2L, ApplicationStatus.PENDING));
        when(responseMapper.toResponse(any())).thenReturn(applicationResponse());
        String body = """
                {"animalId":5,"message":"Please"}
                """;

        mockMvc.perform(post("/api/v1/applications")
                        .with(applicant())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/applications/10"))
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void create_AdminRole_ReturnsForbidden() throws Exception {
        String body = """
                {"animalId":5,"message":"Please"}
                """;

        mockMvc.perform(post("/api/v1/applications")
                        .with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
        verify(applicationService, never()).create(any());
    }

    @Test
    void getAll_AdminRole_ReturnsOk() throws Exception {
        when(applicationService.getAll(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(application(10L, 5L, 2L, ApplicationStatus.PENDING))));
        when(responseMapper.toResponse(any())).thenReturn(applicationResponse());

        mockMvc.perform(get("/api/v1/applications/admin").with(admin()))
                .andExpect(status().isOk());
    }

    @Test
    void getAll_UserRole_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/applications/admin").with(applicant()))
                .andExpect(status().isForbidden());
    }

    @Test
    void getForAdmin_AuthenticatedAdminUser_ReturnsApplication() throws Exception {
        when(applicationService.getForAdmin(10L))
                .thenReturn(application(10L, 5L, 2L, ApplicationStatus.PENDING));
        when(responseMapper.toResponse(any())).thenReturn(applicationResponse());

        mockMvc.perform(get("/api/v1/applications/10").with(applicant()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void approve_AdminRole_ReturnsOk() throws Exception {
        when(applicationService.approve(10L))
                .thenReturn(application(10L, 5L, 2L, ApplicationStatus.APPROVED));
        when(responseMapper.toResponse(any())).thenReturn(applicationResponse());

        mockMvc.perform(patch("/api/v1/applications/admin/10/approve").with(admin()))
                .andExpect(status().isOk());
    }

    @Test
    void reject_AdminRole_ReturnsOk() throws Exception {
        when(applicationService.reject(any(RejectionCommand.class)))
                .thenReturn(application(10L, 5L, 2L, ApplicationStatus.REJECTED));
        when(responseMapper.toResponse(any())).thenReturn(applicationResponse());
        String body = """
                {"decisionComment":"Not a good fit"}
                """;

        mockMvc.perform(patch("/api/v1/applications/admin/10/reject")
                        .with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    void cancel_UserRole_ReturnsOk() throws Exception {
        when(applicationService.cancel(10L, 2L))
                .thenReturn(application(10L, 5L, 2L, ApplicationStatus.CANCELLED));
        when(responseMapper.toResponse(any())).thenReturn(applicationResponse());

        mockMvc.perform(patch("/api/v1/applications/10/cancel").with(applicant()))
                .andExpect(status().isOk());
    }
}
