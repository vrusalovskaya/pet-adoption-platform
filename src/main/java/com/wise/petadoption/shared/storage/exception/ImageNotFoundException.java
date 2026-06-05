package com.wise.petadoption.shared.storage.exception;

import com.wise.petadoption.shared.exception.NotFoundException;

public class ImageNotFoundException extends NotFoundException {

    public ImageNotFoundException(String key) {
        super("Image not found: " + key);
    }
}
