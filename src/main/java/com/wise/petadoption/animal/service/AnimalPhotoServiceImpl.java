package com.wise.petadoption.animal.service;

import com.wise.petadoption.animal.exception.AnimalNotFoundException;
import com.wise.petadoption.animal.exception.AnimalPhotoException;
import com.wise.petadoption.animal.exception.AnimalPhotoNotFoundException;
import com.wise.petadoption.animal.persistence.AnimalEntity;
import com.wise.petadoption.animal.persistence.AnimalRepository;
import com.wise.petadoption.animal.persistence.PhotoMetadata;
import com.wise.petadoption.shared.storage.model.StoredImage;
import com.wise.petadoption.shared.storage.model.StoredImageStream;
import com.wise.petadoption.shared.storage.service.ImageStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class AnimalPhotoServiceImpl implements AnimalPhotoService {

    private final AnimalRepository animalRepository;
    private final ImageStorage imageStorage;

    @Override
    @Transactional
    public void replacePhoto(Long animalId, MultipartFile file) {
        AnimalEntity animal = getEntityById(animalId);
        try (InputStream in = file.getInputStream()) {
            validateFile(file);
            StoredImage image = imageStorage.save(in, file.getSize(),
                    file.getContentType(), file.getOriginalFilename());

            if (animal.getPhotoMetadata() != null && animal.getPhotoMetadata().getPhotoFileId() != null) {
                imageStorage.delete(animal.getPhotoMetadata().getPhotoFileId());
            }

            animal.setPhotoMetadata(new PhotoMetadata(image.key(), imageStorage.type(), image.contentType(),
                    image.originalFilename(), image.sizeBytes()));
        } catch (IOException e) {
            throw new AnimalPhotoException("Failed to upload photo for animal " + animalId, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public StoredImageStream download(Long animalId) {
        AnimalEntity animal = getEntityById(animalId);
        PhotoMetadata photo = getRequiredPhoto(animal);
        return imageStorage.load(photo.getPhotoFileId());
    }

    @Override
    @Transactional
    public void delete(Long animalId) {
        AnimalEntity animal = getEntityById(animalId);
        PhotoMetadata photo = getRequiredPhoto(animal);
        imageStorage.delete(photo.getPhotoFileId());
        animal.setPhotoMetadata(null);
    }

    private AnimalEntity getEntityById(Long id) {
        return animalRepository.findById(id).orElseThrow(() -> new AnimalNotFoundException(id));
    }

    private PhotoMetadata getRequiredPhoto(AnimalEntity animal) {
        PhotoMetadata photo = animal.getPhotoMetadata();
        if (photo == null || photo.getPhotoFileId() == null) {
            throw new AnimalPhotoNotFoundException(animal.getId());
        }
        return photo;
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new AnimalPhotoException("Uploaded file is empty");
        }

        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new AnimalPhotoException("Only image files are allowed");
        }
    }
}
