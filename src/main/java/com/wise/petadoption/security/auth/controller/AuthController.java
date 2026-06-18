package com.wise.petadoption.security.auth.controller;

import com.wise.petadoption.security.auth.service.AuthenticationFacade;
import com.wise.petadoption.security.auth.service.LoginCommand;
import com.wise.petadoption.security.auth.service.RegisterCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
        return new AuthResponse(auth.login(toCommand(request)));
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return new AuthResponse(auth.register(toCommand(request)));
    }

    private RegisterCommand toCommand(RegisterRequest request) {
        return new RegisterCommand(request.email(), request.password(), request.firstName(),
                request.lastName(), request.phone());
    }

    private LoginCommand toCommand(LoginRequest request) {
        return new LoginCommand(request.email(), request.password());
    }
}
