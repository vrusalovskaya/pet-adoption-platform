package com.wise.petadoption.animal.persistence;

import com.wise.petadoption.shared.storage.model.StorageType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PhotoMetadata {
    @Column(length = 100)
    private String photoFileId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private StorageType storageType;

    private String photoContentType;

    private String photoFilename;

    private Long sizeBytes;
}
