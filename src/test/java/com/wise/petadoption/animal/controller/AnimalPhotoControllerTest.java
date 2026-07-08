package com.wise.petadoption.animal.controller;

import com.wise.petadoption.animal.service.AnimalPhotoService;
import com.wise.petadoption.security.domain.SecurityUser;
import com.wise.petadoption.shared.storage.model.StoredImageStream;
import com.wise.petadoption.support.WebMvcTestSecurityConfig;
import com.wise.petadoption.user.common.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static com.wise.petadoption.support.TestFixtures.securityUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

@WebMvcTest(AnimalPhotoController.class)
@Import(WebMvcTestSecurityConfig.class)
class AnimalPhotoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnimalPhotoService animalPhotoService;

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
    void upload_AdminUser_ReturnsAccepted() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "rex.png", "image/png", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/v1/animals/1/photo").file(file).with(admin()))
                .andExpect(status().isNoContent());
        verify(animalPhotoService).replacePhoto(eq(1L), any());
    }

    @Test
    void upload_NonAdminUser_ReturnsForbidden() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "rex.png", "image/png", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/v1/animals/1/photo").file(file).with(regularUser()))
                .andExpect(status().isForbidden());
        verify(animalPhotoService, never()).replacePhoto(any(), any());
    }

    @Test
    void download_ExistingPhoto_ReturnsImageStream() throws Exception {
        byte[] data = "hello".getBytes(StandardCharsets.UTF_8);
        when(animalPhotoService.download(1L)).thenReturn(new StoredImageStream(
                new ByteArrayInputStream(data), "image/png", "rex.png", data.length));

        MvcResult result = mockMvc.perform(get("/api/v1/animals/1/photo"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(content().bytes(data));
    }

    @Test
    void delete_AdminUser_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/animals/1/photo").with(admin()))
                .andExpect(status().isNoContent());
        verify(animalPhotoService).delete(1L);
    }
}
