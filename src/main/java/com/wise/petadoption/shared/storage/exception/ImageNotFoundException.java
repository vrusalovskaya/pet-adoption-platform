package com.wise.petadoption.shared.storage.exception;

public class ImageNotFoundException extends NotFoundException {

    public ImageNotFoundException(String key) {
        super("Image not found: " + key);
    }
}
