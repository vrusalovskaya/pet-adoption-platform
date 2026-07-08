package com.wise.petadoption.shared.storage.contract;

import com.wise.petadoption.shared.storage.model.StorageType;
import com.wise.petadoption.shared.storage.model.StoredImage;
import com.wise.petadoption.shared.storage.model.StoredImageStream;

import java.io.InputStream;

public interface ImageStorage {
    StoredImage save(InputStream content, long sizeBytes, String contentType, String originalFilename);

    StoredImageStream load(String key);

    void delete(String key);

    StorageType type();
}
