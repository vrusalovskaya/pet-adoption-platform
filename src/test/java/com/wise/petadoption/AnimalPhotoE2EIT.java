package com.wise.petadoption;

import com.jayway.jsonpath.JsonPath;
import com.wise.petadoption.support.AbstractFullStackIT;
import com.wise.petadoption.user.common.Role;
import com.wise.petadoption.user.domain.CreateUserCommand;
import com.wise.petadoption.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

@SpringBootTest
@AutoConfigureMockMvc
class AnimalPhotoE2EIT extends AbstractFullStackIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserService userService;

    private String adminToken() throws Exception {
        userService.create(new CreateUserCommand("admin-e2e@example.com", "adminpass1",
                "System", "Admin", null, Role.ROLE_ADMIN));
        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"admin-e2e@example.com","password":"adminpass1"}
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.accessToken");
    }

    private long createShelter(String token) throws Exception {
        String response = mockMvc.perform(post("/api/v1/shelters")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"E2E Shelter","city":"E2ECity","address":"1 Main St",
                                 "contactEmail":"e2e-shelter@example.com","contactPhone":"+15557654321",
                                 "description":"desc"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    private long createAnimal(String token, long shelterId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/animals")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"shelterId":%d,"name":"Rex","species":"DOG","breed":"Labrador",
                                 "birthYear":2020,"gender":"MALE","description":"Good boy"}
                                """.formatted(shelterId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(response, "$.id")).longValue();
    }

    @Test
    void photoLifecycle_UploadDownloadDelete_Succeeds() throws Exception {
        String token = adminToken();
        long shelterId = createShelter(token);
        long animalId = createAnimal(token, shelterId);
        byte[] imageBytes = "real-image-content".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("file", "rex.png", "image/png", imageBytes);

        mockMvc.perform(multipart("/api/v1/animals/{id}/photo", animalId)
                        .file(file)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        MvcResult downloadResult = mockMvc.perform(get("/api/v1/animals/{id}/photo", animalId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(request().asyncStarted())
                .andReturn();
        mockMvc.perform(asyncDispatch(downloadResult))
                .andExpect(status().isOk())
                .andExpect(content().bytes(imageBytes));

        mockMvc.perform(delete("/api/v1/animals/{id}/photo", animalId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/animals/{id}/photo", animalId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}
