package com.wise.petadoption.user.exception;

import com.wise.petadoption.shared.exception.NotFoundException;

public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(Long id) {
        super("User with id " + id + " not found");
    }
}
