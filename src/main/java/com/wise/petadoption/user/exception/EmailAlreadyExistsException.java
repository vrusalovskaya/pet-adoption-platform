package com.wise.petadoption.user.exception;

import com.wise.petadoption.shared.exception.ConflictException;

public class EmailAlreadyExistsException extends ConflictException {

    public EmailAlreadyExistsException(String email) {
        super("Email '%s' is already in use".formatted(email));
    }
}