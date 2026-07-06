package com.wise.petadoption.security.service;

import com.wise.petadoption.security.domain.*;
import com.wise.petadoption.security.jwt.JwtService;
import com.wise.petadoption.user.common.Role;
import com.wise.petadoption.user.domain.CreateUserCommand;
import com.wise.petadoption.user.domain.User;
import com.wise.petadoption.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;

import static com.wise.petadoption.support.TestFixtures.securityUser;
import static com.wise.petadoption.support.TestFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationFacadeImplTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private UserService userService;

    @InjectMocks
    private AuthenticationFacadeImpl authenticationFacade;

    @Test
    void login_ValidCredentials_ReturnsAccessAndRefreshTokens() {
        LoginCommand command = new LoginCommand("jane@example.com", "raw-password");
        SecurityUser principal = securityUser(1L, "jane@example.com", Role.ROLE_USER);
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(jwtService.generateAccessToken(principal)).thenReturn("access-token");
        when(refreshTokenService.create(1L)).thenReturn("refresh-token");

        AuthenticationResult result = authenticationFacade.login(command);

        assertThat(result).isEqualTo(new AuthenticationResult("access-token", "refresh-token"));
    }

    @Test
    void login_PrincipalIsNotSecurityUser_ThrowsIllegalStateException() {
        LoginCommand command = new LoginCommand("jane@example.com", "raw-password");
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("not-a-security-user");

        assertThatThrownBy(() -> authenticationFacade.login(command))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void register_NewUser_CreatesUserWithUserRoleAndReturnsTokens() {
        RegisterCommand command = new RegisterCommand("jane@example.com", "raw-password",
                "Jane", "Doe", "+15551234567");
        User createdUser = user(1L, "jane@example.com", Role.ROLE_USER);
        when(userService.create(any(CreateUserCommand.class))).thenReturn(createdUser);
        when(jwtService.generateAccessToken(any(SecurityUser.class))).thenReturn("access-token");
        when(refreshTokenService.create(1L)).thenReturn("refresh-token");

        AuthenticationResult result = authenticationFacade.register(command);

        assertThat(result).isEqualTo(new AuthenticationResult("access-token", "refresh-token"));
        ArgumentCaptor<CreateUserCommand> captor = ArgumentCaptor.forClass(CreateUserCommand.class);
        verify(userService).create(captor.capture());
        assertThat(captor.getValue().role()).isEqualTo(Role.ROLE_USER);
        assertThat(captor.getValue().email()).isEqualTo("jane@example.com");
    }

    @Test
    void refresh_ValidToken_RotatesTokenAndIssuesNewAccessToken() {
        RefreshCommand command = new RefreshCommand("old-refresh");
        when(refreshTokenService.rotate("old-refresh"))
                .thenReturn(new RefreshTokenRotationResult("new-refresh", 1L));
        when(userService.findById(1L)).thenReturn(user(1L, "jane@example.com", Role.ROLE_USER));
        when(jwtService.generateAccessToken(any(SecurityUser.class))).thenReturn("access-token");

        AuthenticationResult result = authenticationFacade.refresh(command);

        assertThat(result).isEqualTo(new AuthenticationResult("access-token", "new-refresh"));
    }
}
