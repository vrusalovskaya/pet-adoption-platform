package com.wise.petadoption.security.auth.service;

import com.wise.petadoption.security.jwt.JwtService;
import com.wise.petadoption.security.SecurityUser;
import com.wise.petadoption.user.domain.CreateUserCommand;
import com.wise.petadoption.user.common.Role;
import com.wise.petadoption.user.domain.User;
import com.wise.petadoption.user.service.UserService;
import lombok.RequiredArgsConstructor;
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
    private final UserService userService;

    @Override
    public String login(LoginCommand command) {
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(command.email(), command.password())
                );

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof SecurityUser user)) {
            throw new IllegalStateException("Authenticated principal is not SecurityUser");
        }

        return jwtService.generateToken(user);

    }

    @Override
    @Transactional
    public String register(RegisterCommand command) {
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
        return jwtService.generateToken(principal);
    }
}
