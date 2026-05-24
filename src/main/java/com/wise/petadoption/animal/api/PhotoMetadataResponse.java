package com.wise.petadoption.animal.api;

import com.wise.petadoption.shared.StorageType;

public record PhotoMetadataResponse(
        String photoFileId,
        StorageType storageType,
        String photoContentType,
        String photoFilename,
        Long sizeBytes
) {
}
