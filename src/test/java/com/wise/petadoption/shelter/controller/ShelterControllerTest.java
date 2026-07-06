package com.wise.petadoption.shelter.controller;

import com.wise.petadoption.security.domain.SecurityUser;
import com.wise.petadoption.shelter.api.ShelterResponse;
import com.wise.petadoption.shelter.domain.ModifyShelterCommand;
import com.wise.petadoption.shelter.mapper.ShelterResponseMapper;
import com.wise.petadoption.shelter.service.ShelterService;
import com.wise.petadoption.support.TestFixtures;
import com.wise.petadoption.support.WebMvcTestSecurityConfig;
import com.wise.petadoption.user.common.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static com.wise.petadoption.support.TestFixtures.securityUser;
import static com.wise.petadoption.support.TestFixtures.shelter;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShelterController.class)
@Import(WebMvcTestSecurityConfig.class)
class ShelterControllerTest {

    private static final String SHELTER_BODY = """
            {"name":"Happy Tails","city":"Springfield","address":"1 Main St",
             "contactEmail":"shelter@example.com","contactPhone":"+15557654321",
             "description":"A cozy shelter"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShelterService shelterService;
    @MockitoBean
    private ShelterResponseMapper responseMapper;

    private ShelterResponse shelterResponse() {
        return new ShelterResponse(1L, "Happy Tails", "Springfield", "1 Main St",
                "shelter@example.com", "+15557654321", "A cozy shelter", false, TestFixtures.NOW);
    }

    private RequestPostProcessor admin() {
        SecurityUser principal = securityUser(1L, "admin@example.com", Role.ROLE_ADMIN);
        return authentication(new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()));
    }

    private RequestPostProcessor regularUser() {
        SecurityUser principal = securityUser(2L, "user@example.com", Role.ROLE_USER);
        return authentication(new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()));
    }

    @Test
    void get_ExistingId_ReturnsShelter() throws Exception {
        when(shelterService.get(1L)).thenReturn(shelter(1L));
        when(responseMapper.toResponse(any())).thenReturn(shelterResponse());

        mockMvc.perform(get("/api/v1/shelters/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Happy Tails"));
    }

    @Test
    void create_AdminUser_ReturnsCreatedWithLocation() throws Exception {
        when(shelterService.create(any(ModifyShelterCommand.class))).thenReturn(shelter(1L));
        when(responseMapper.toResponse(any())).thenReturn(shelterResponse());

        mockMvc.perform(post("/api/v1/shelters")
                        .with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SHELTER_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/shelters/1"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void create_NonAdminUser_ReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/shelters")
                        .with(regularUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SHELTER_BODY))
                .andExpect(status().isForbidden());
        verify(shelterService, never()).create(any());
    }

    @Test
    void create_AnonymousUser_ReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/shelters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SHELTER_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_InvalidRequest_ReturnsBadRequest() throws Exception {
        String invalidBody = """
                {"name":"Happy Tails","address":"1 Main St","contactEmail":"not-an-email"}
                """;

        mockMvc.perform(post("/api/v1/shelters")
                        .with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_AdminUser_ReturnsOk() throws Exception {
        when(shelterService.update(any(ModifyShelterCommand.class))).thenReturn(shelter(1L));
        when(responseMapper.toResponse(any())).thenReturn(shelterResponse());

        mockMvc.perform(put("/api/v1/shelters/1")
                        .with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SHELTER_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void verify_AdminUser_ReturnsOk() throws Exception {
        when(shelterService.verify(1L)).thenReturn(shelter(1L));
        when(responseMapper.toResponse(any())).thenReturn(shelterResponse());

        mockMvc.perform(patch("/api/v1/shelters/1/verify")
                        .with(admin()))
                .andExpect(status().isOk());
    }

    @Test
    void delete_AdminUser_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/shelters/1")
                        .with(admin()))
                .andExpect(status().isNoContent());
        verify(shelterService).delete(1L);
    }
}
