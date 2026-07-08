package com.wise.petadoption.adoption.exception;

public class ApplicationAccessDeniedException extends RuntimeException {
    public ApplicationAccessDeniedException() {
        super("You do not have permission to access this application");
    }
}
