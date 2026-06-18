package com.wise.petadoption.animal.exception;


import com.wise.petadoption.shared.exception.NotFoundException;

public class AnimalNotFoundException extends NotFoundException {
    public AnimalNotFoundException(Long id) {
        super("Animal with id " + id + " not found");
    }
}
