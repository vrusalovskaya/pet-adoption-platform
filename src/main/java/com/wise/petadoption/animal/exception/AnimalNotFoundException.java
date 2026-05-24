package com.wise.petadoption.animal.exception;

public class AnimalNotFoundException extends RuntimeException {
    public AnimalNotFoundException(Long id) {
        super("Animal with id " + id + " not found");
    }
}
