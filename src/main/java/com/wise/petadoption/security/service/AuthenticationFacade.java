package com.wise.petadoption.security.service;

import com.wise.petadoption.security.domain.AuthenticationResult;
import com.wise.petadoption.security.domain.LoginCommand;
import com.wise.petadoption.security.domain.RegisterCommand;
import com.wise.petadoption.security.domain.LogoutCommand;
import com.wise.petadoption.security.domain.RefreshCommand;

public interface AuthenticationFacade {
    AuthenticationResult login(LoginCommand command);

    AuthenticationResult register(RegisterCommand command);

    AuthenticationResult refresh(RefreshCommand command);

    void logout(LogoutCommand command);

    void logoutEverywhere(Long userId);
}
