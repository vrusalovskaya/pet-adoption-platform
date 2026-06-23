package com.wise.petadoption.security.controller;

import com.wise.petadoption.security.domain.SecurityUser;
import com.wise.petadoption.security.api.AuthResponse;
import com.wise.petadoption.security.api.LoginRequest;
import com.wise.petadoption.security.api.RegisterRequest;
import com.wise.petadoption.security.service.AuthenticationFacade;
import com.wise.petadoption.security.domain.AuthenticationResult;
import com.wise.petadoption.security.domain.LogoutCommand;
import com.wise.petadoption.security.api.LogoutRequest;
import com.wise.petadoption.security.domain.RefreshCommand;
import com.wise.petadoption.security.api.RefreshRequest;
import com.wise.petadoption.security.domain.LoginCommand;
import com.wise.petadoption.security.domain.RegisterCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationFacade auth;

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        AuthenticationResult result = auth.login(toCommand(request));
        return new AuthResponse(result.accessToken(), result.refreshToken());
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        AuthenticationResult result = auth.register(toCommand(request));
        return new AuthResponse(result.accessToken(), result.refreshToken());
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
        AuthenticationResult result = auth.refresh(toCommand(request));
        return new AuthResponse(result.accessToken(), result.refreshToken());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        auth.logout(toCommand(request));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutEverywhere(@AuthenticationPrincipal SecurityUser user) {
        auth.logoutEverywhere(user.getUserId());
        return ResponseEntity.noContent().build();
    }

    private RegisterCommand toCommand(RegisterRequest request) {
        return new RegisterCommand(request.email(), request.password(), request.firstName(),
                request.lastName(), request.phone());
    }

    private LoginCommand toCommand(LoginRequest request) {
        return new LoginCommand(request.email(), request.password());
    }

    private RefreshCommand toCommand(RefreshRequest request) {
        return new RefreshCommand(request.refreshToken());
    }

    private LogoutCommand toCommand(LogoutRequest request) {
        return new LogoutCommand(request.refreshToken());
    }
}
