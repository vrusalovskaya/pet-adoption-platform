package com.wise.petadoption.animal.exception;

import com.wise.petadoption.shared.exception.ConflictException;

public class AnimalNotAvailableException extends ConflictException {
    public AnimalNotAvailableException(Long animalId) {
        super("Animal with id %d is not available for adoption".formatted(animalId));
    }
}
