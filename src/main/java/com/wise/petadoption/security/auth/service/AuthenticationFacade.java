package com.wise.petadoption.security.auth.service;

import com.wise.petadoption.security.auth.AuthenticationResult;
import com.wise.petadoption.security.auth.refresh.LogoutCommand;
import com.wise.petadoption.security.auth.refresh.RefreshCommand;

public interface AuthenticationFacade {
    AuthenticationResult login(LoginCommand command);

    AuthenticationResult register(RegisterCommand command);

    AuthenticationResult refresh(RefreshCommand command);

    void logout(LogoutCommand command);

    void logoutEverywhere(Long userId);
}
