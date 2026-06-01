package com.wise.petadoption.shelter.exception;

import com.wise.petadoption.shared.storage.exception.NotFoundException;

public class ShelterNotFoundException extends NotFoundException {

    public ShelterNotFoundException(Long id) {
        super("Shelter with id " + id + " not found");
    }
}
