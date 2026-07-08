package com.wise.petadoption.animal.service;

import com.wise.petadoption.shared.storage.model.StoredImageStream;
import org.springframework.web.multipart.MultipartFile;

public interface AnimalPhotoService {
    void replacePhoto(Long animalId, MultipartFile file);

    StoredImageStream download(Long animalId);

    void delete(Long animalId);
}
