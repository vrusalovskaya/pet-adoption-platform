package com.wise.petadoption.security.auth.service;

public interface AuthenticationFacade {
    String login(LoginCommand command);

    String register(RegisterCommand command);
}
