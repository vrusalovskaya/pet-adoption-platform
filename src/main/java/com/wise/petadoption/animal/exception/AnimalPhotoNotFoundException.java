package com.wise.petadoption.animal.exception;

import com.wise.petadoption.shared.exception.NotFoundException;

public class AnimalPhotoNotFoundException extends NotFoundException {
    public AnimalPhotoNotFoundException(Long animalId) {
        super("Photo for animal with id " + animalId + " not found");
    }
}
