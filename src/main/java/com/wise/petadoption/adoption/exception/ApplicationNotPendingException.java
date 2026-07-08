package com.wise.petadoption.adoption.exception;

import com.wise.petadoption.adoption.common.ApplicationStatus;
import com.wise.petadoption.shared.exception.ConflictException;

public class ApplicationNotPendingException extends ConflictException {

    public ApplicationNotPendingException(ApplicationStatus currentStatus) {
        super("Application must be in PENDING status, current status is %s"
                .formatted(currentStatus));
    }
}
