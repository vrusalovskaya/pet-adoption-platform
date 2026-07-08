package com.wise.petadoption.animal.controller;

import com.wise.petadoption.animal.api.AnimalResponse;
import com.wise.petadoption.animal.common.AnimalStatus;
import com.wise.petadoption.animal.common.Gender;
import com.wise.petadoption.animal.common.Species;
import com.wise.petadoption.animal.domain.CreateAnimalCommand;
import com.wise.petadoption.animal.domain.UpdateAnimalCommand;
import com.wise.petadoption.animal.mapper.AnimalResponseMapper;
import com.wise.petadoption.animal.service.AnimalService;
import com.wise.petadoption.security.domain.SecurityUser;
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

import static com.wise.petadoption.support.TestFixtures.animal;
import static com.wise.petadoption.support.TestFixtures.securityUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnimalController.class)
@Import(WebMvcTestSecurityConfig.class)
class AnimalControllerTest {

    private static final String ANIMAL_BODY = """
            {"shelterId":7,"name":"Rex","species":"DOG","breed":"Labrador",
             "birthYear":2020,"gender":"MALE","description":"Good boy"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnimalService animalService;
    @MockitoBean
    private AnimalResponseMapper responseMapper;

    private AnimalResponse animalResponse() {
        return new AnimalResponse(1L, 7L, "Rex", Species.DOG, "Labrador", 2020,
                Gender.MALE, "Good boy", AnimalStatus.AVAILABLE, null, TestFixtures.NOW);
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
    void get_ExistingId_ReturnsAnimal() throws Exception {
        when(animalService.get(1L)).thenReturn(animal(1L, AnimalStatus.AVAILABLE, 7L));
        when(responseMapper.toResponse(any())).thenReturn(animalResponse());

        mockMvc.perform(get("/api/v1/animals/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Rex"));
    }

    @Test
    void create_AdminUser_ReturnsCreatedWithLocation() throws Exception {
        when(animalService.create(any(CreateAnimalCommand.class)))
                .thenReturn(animal(1L, AnimalStatus.AVAILABLE, 7L));
        when(responseMapper.toResponse(any())).thenReturn(animalResponse());

        mockMvc.perform(post("/api/v1/animals")
                        .with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ANIMAL_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/animals/1"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void create_NonAdminUser_ReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/animals")
                        .with(regularUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ANIMAL_BODY))
                .andExpect(status().isForbidden());
        verify(animalService, never()).create(any());
    }

    @Test
    void create_InvalidRequest_ReturnsBadRequest() throws Exception {
        String invalidBody = """
                {"shelterId":7,"breed":"Labrador","birthYear":2020}
                """;

        mockMvc.perform(post("/api/v1/animals")
                        .with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_AdminUser_ReturnsOk() throws Exception {
        when(animalService.update(any(UpdateAnimalCommand.class)))
                .thenReturn(animal(1L, AnimalStatus.AVAILABLE, 7L));
        when(responseMapper.toResponse(any())).thenReturn(animalResponse());

        mockMvc.perform(put("/api/v1/animals/1")
                        .with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ANIMAL_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateStatus_AdminUser_ReturnsOk() throws Exception {
        when(animalService.setStatus(eq(1L), eq(AnimalStatus.RESERVED)))
                .thenReturn(animal(1L, AnimalStatus.RESERVED, 7L));
        when(responseMapper.toResponse(any())).thenReturn(animalResponse());
        String body = """
                {"status":"RESERVED"}
                """;

        mockMvc.perform(patch("/api/v1/animals/1/status")
                        .with(admin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    void delete_AdminUser_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/animals/1")
                        .with(admin()))
                .andExpect(status().isNoContent());
        verify(animalService).delete(1L);
    }
}
