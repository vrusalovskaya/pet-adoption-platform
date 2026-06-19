package com.wise.petadoption.user.service;

import com.wise.petadoption.user.domain.ChangePasswordCommand;
import com.wise.petadoption.user.domain.CreateUserCommand;
import com.wise.petadoption.user.domain.UpdateProfileCommand;
import com.wise.petadoption.user.domain.User;

import java.util.Optional;

public interface UserService {
    User create(CreateUserCommand command);

    Optional<User> findByEmail(String email);

    User updateProfile(Long id, UpdateProfileCommand request);

    void changePassword(Long userId, ChangePasswordCommand command);

    void delete(Long id);
}
