package com.wise.petadoption.security.controller;

import com.wise.petadoption.security.domain.*;
import com.wise.petadoption.security.service.AuthenticationFacade;
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

import static com.wise.petadoption.support.TestFixtures.securityUser;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(WebMvcTestSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationFacade authenticationFacade;

    @Test
    void login_ValidRequest_ReturnsTokens() throws Exception {
        when(authenticationFacade.login(any(LoginCommand.class)))
                .thenReturn(new AuthenticationResult("access-token", "refresh-token"));
        String body = """
                {"email":"jane@example.com","password":"password1"}
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void login_InvalidEmail_ReturnsBadRequest() throws Exception {
        String body = """
                {"email":"not-an-email","password":"password1"}
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_ValidRequest_ReturnsTokens() throws Exception {
        when(authenticationFacade.register(any(RegisterCommand.class)))
                .thenReturn(new AuthenticationResult("access-token", "refresh-token"));
        String body = """
                {"email":"jane@example.com","password":"password1","firstName":"Jane",
                 "lastName":"Doe","phone":"+15551234567"}
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"));
    }

    @Test
    void refresh_ValidRequest_ReturnsTokens() throws Exception {
        when(authenticationFacade.refresh(any(RefreshCommand.class)))
                .thenReturn(new AuthenticationResult("new-access", "new-refresh"));
        String body = """
                {"refreshToken":"some-refresh-token"}
                """;

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshToken").value("new-refresh"));
    }

    @Test
    void logout_ValidRequest_ReturnsNoContent() throws Exception {
        String body = """
                {"refreshToken":"some-refresh-token"}
                """;

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNoContent());
        verify(authenticationFacade).logout(any(LogoutCommand.class));
    }

    @Test
    void logoutEverywhere_AuthenticatedUser_ReturnsNoContentAndRevokesTokens() throws Exception {
        var principal = securityUser(7L, "jane@example.com", Role.ROLE_USER);

        mockMvc.perform(post("/api/v1/auth/logout-all")
                        .with(authentication(new UsernamePasswordAuthenticationToken(
                                principal, null, principal.getAuthorities()))))
                .andExpect(status().isNoContent());
        verify(authenticationFacade).logoutEverywhere(7L);
    }
}
