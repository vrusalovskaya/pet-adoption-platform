package com.wise.petadoption.user.controller;

import com.wise.petadoption.security.domain.SecurityUser;
import com.wise.petadoption.support.WebMvcTestSecurityConfig;
import com.wise.petadoption.user.api.UserResponse;
import com.wise.petadoption.user.common.Role;
import com.wise.petadoption.user.domain.ChangePasswordCommand;
import com.wise.petadoption.user.domain.UpdateProfileCommand;
import com.wise.petadoption.user.mapper.UserResponseMapper;
import com.wise.petadoption.user.service.UserService;
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
import static com.wise.petadoption.support.TestFixtures.user;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(WebMvcTestSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private UserResponseMapper responseMapper;

    private RequestPostProcessor authenticatedUser() {
        SecurityUser principal = securityUser(7L, "jane@example.com", Role.ROLE_USER);
        return authentication(new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()));
    }

    @Test
    void updateProfile_AuthenticatedUser_ReturnsUpdatedProfile() throws Exception {
        when(userService.updateProfile(any(UpdateProfileCommand.class)))
                .thenReturn(user(7L, "jane@example.com", Role.ROLE_USER));
        when(responseMapper.toResponse(any()))
                .thenReturn(new UserResponse(7L, "jane@example.com", "Jane", "Doe", "+15551234567"));
        String body = """
                {"email":"jane@example.com","firstName":"Jane","lastName":"Doe","phone":"+15551234567"}
                """;

        mockMvc.perform(patch("/api/v1/users/me")
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.email").value("jane@example.com"));
    }

    @Test
    void updateProfile_InvalidEmail_ReturnsBadRequest() throws Exception {
        String body = """
                {"email":"not-an-email","firstName":"Jane","lastName":"Doe","phone":"+1"}
                """;

        mockMvc.perform(patch("/api/v1/users/me")
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_ValidRequest_ReturnsNoContent() throws Exception {
        String body = """
                {"oldPassword":"oldpass12","newPassword":"newpass34"}
                """;

        mockMvc.perform(patch("/api/v1/users/me/password")
                        .with(authenticatedUser())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());
        verify(userService).changePassword(any(ChangePasswordCommand.class));
    }

    @Test
    void delete_AuthenticatedUser_ReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/users/me")
                        .with(authenticatedUser()))
                .andExpect(status().isNoContent());
        verify(userService).delete(7L);
    }
}
