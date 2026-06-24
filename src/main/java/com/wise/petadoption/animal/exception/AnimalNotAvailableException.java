package com.wise.petadoption.animal.exception;

public class AnimalNotAvailableException extends RuntimeException {
    public AnimalNotAvailableException(Long animalId) {
        super("Animal with id %d is not available for adoption".formatted(animalId));
    }
}
