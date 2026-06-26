package com.wise.petadoption.animal.exception;

import com.wise.petadoption.animal.common.AnimalStatus;
import com.wise.petadoption.shared.exception.ConflictException;

public class NotValidAnimalStatusTransitionException extends ConflictException {

    public NotValidAnimalStatusTransitionException(AnimalStatus previousStatus, AnimalStatus newStatus) {
        super("Cannot perform transition from " + previousStatus + " to "  + newStatus);
    }
}
