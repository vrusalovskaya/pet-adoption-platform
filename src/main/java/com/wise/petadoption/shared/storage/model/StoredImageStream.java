package com.wise.petadoption.shared.storage.model;

import java.io.IOException;
import java.io.InputStream;

public record StoredImageStream(
        InputStream stream,
        String contentType,
        String originalFilename,
        long sizeBytes
) implements AutoCloseable {

    @Override
    public void close() throws IOException {
        stream.close();
    }
}
