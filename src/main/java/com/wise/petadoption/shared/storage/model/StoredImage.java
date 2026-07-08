package com.wise.petadoption.shared.storage.model;

public record StoredImage(
        String key,
        String contentType,
        String originalFilename,
        long sizeBytes
) {
}
