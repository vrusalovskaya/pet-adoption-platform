package com.wise.petadoption.adoption.exception;

import com.wise.petadoption.adoption.common.ApplicationStatus;

public class ApplicationNotPendingException extends RuntimeException {

    public ApplicationNotPendingException(ApplicationStatus currentStatus) {
        super("Application must be in PENDING status, current status is %s"
                .formatted(currentStatus));
    }
}
