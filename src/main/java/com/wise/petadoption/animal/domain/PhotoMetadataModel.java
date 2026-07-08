package com.wise.petadoption.animal.domain;

import com.wise.petadoption.shared.storage.model.StorageType;

public record PhotoMetadataModel(
        String photoFileId,
        StorageType storageType,
        String photoContentType,
        String photoFilename,
        Long sizeBytes
) {
}
