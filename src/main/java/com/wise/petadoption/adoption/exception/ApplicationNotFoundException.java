package com.wise.petadoption.adoption.exception;

import com.wise.petadoption.shared.exception.NotFoundException;

public class ApplicationNotFoundException extends NotFoundException {
    public ApplicationNotFoundException(Long id) {
        super("Application with id " + id + " not found");
    }
}

