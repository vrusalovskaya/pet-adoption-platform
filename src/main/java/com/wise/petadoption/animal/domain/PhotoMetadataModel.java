package com.wise.petadoption.animal.domain;

import com.wise.petadoption.shared.StorageType;

public record PhotoMetadataModel(
        String photoFileId,
        StorageType storageType,
        String photoContentType,
        String photoFilename,
        Long sizeBytes
) {
}
