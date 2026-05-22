package com.wise.petadoption.shelter.exception;

public class ShelterNotFoundException extends RuntimeException {

    public ShelterNotFoundException(Long id) {
        super("Shelter with id " + id + " not found");
    }
}
