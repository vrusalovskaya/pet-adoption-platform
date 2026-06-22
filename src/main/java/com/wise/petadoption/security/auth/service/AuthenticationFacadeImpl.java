package com.wise.petadoption.security.auth.service;

import com.wise.petadoption.security.SecurityUser;
import com.wise.petadoption.security.auth.AuthenticationResult;
import com.wise.petadoption.security.auth.refresh.LogoutCommand;
import com.wise.petadoption.security.auth.refresh.RefreshCommand;
import com.wise.petadoption.security.auth.refresh.RefreshToken;
import com.wise.petadoption.security.auth.refresh.RefreshTokenService;
import com.wise.petadoption.security.jwt.JwtService;
import com.wise.petadoption.user.common.Role;
import com.wise.petadoption.user.domain.CreateUserCommand;
import com.wise.petadoption.user.domain.User;
import com.wise.petadoption.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class AuthenticationFacadeImpl implements AuthenticationFacade {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    @Override
    public AuthenticationResult login(LoginCommand command) {
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(command.email(), command.password())
                );

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof SecurityUser user)) {
            throw new IllegalStateException("Authenticated principal is not SecurityUser");
        }

        return getAuthenticationResult(user);
    }

    @Override
    @Transactional
    public AuthenticationResult register(RegisterCommand command) {
        User user = userService.create(
                new CreateUserCommand(
                        command.email(),
                        command.password(),
                        command.firstName(),
                        command.lastName(),
                        command.phone(),
                        Role.ROLE_USER
                )
        );

        SecurityUser principal = SecurityUser.from(user);
        return getAuthenticationResult(principal);
    }

    @Override
    @Transactional
    public AuthenticationResult refresh(RefreshCommand command) {
        RefreshToken refreshToken = refreshTokenService.findValidToken(command.refreshToken());

        User user = userService.findById(refreshToken.userId());
        SecurityUser principal = SecurityUser.from(user);

        AuthenticationResult result = getAuthenticationResult(principal);
        refreshTokenService.deleteByToken(command.refreshToken());

        return result;
    }

    @Override
    public void logout(LogoutCommand command) {
        refreshTokenService.deleteByToken(command.refreshToken());
    }

    @Override
    public void logoutEverywhere(Long userId) {
        refreshTokenService.deleteByUserId(userId);
    }

    private @NonNull AuthenticationResult getAuthenticationResult(SecurityUser user) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.create(user.getUserId());

        return new AuthenticationResult(accessToken, refreshToken);
    }
}
