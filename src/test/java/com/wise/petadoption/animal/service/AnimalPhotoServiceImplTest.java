package com.wise.petadoption.animal.service;

import com.wise.petadoption.animal.common.AnimalStatus;
import com.wise.petadoption.animal.exception.AnimalNotFoundException;
import com.wise.petadoption.animal.exception.AnimalPhotoException;
import com.wise.petadoption.animal.exception.AnimalPhotoNotFoundException;
import com.wise.petadoption.animal.persistence.AnimalEntity;
import com.wise.petadoption.animal.persistence.AnimalRepository;
import com.wise.petadoption.animal.persistence.PhotoMetadata;
import com.wise.petadoption.shared.storage.contract.ImageStorage;
import com.wise.petadoption.shared.storage.model.StorageType;
import com.wise.petadoption.shared.storage.model.StoredImage;
import com.wise.petadoption.shared.storage.model.StoredImageStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static com.wise.petadoption.support.TestFixtures.animalEntity;
import static com.wise.petadoption.support.TestFixtures.shelterEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnimalPhotoServiceImplTest {

    @Mock
    private AnimalRepository animalRepository;
    @Mock
    private ImageStorage imageStorage;

    @InjectMocks
    private AnimalPhotoServiceImpl animalPhotoService;

    private AnimalEntity availableAnimal() {
        return animalEntity(1L, AnimalStatus.AVAILABLE, shelterEntity(7L));
    }

    @Test
    void replacePhoto_ValidImageWithoutExistingPhoto_StoresMetadata() throws IOException {
        AnimalEntity animal = availableAnimal();
        MultipartFile file = imageFile("image/png", "rex.png", 100L);
        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));
        when(imageStorage.save(any(InputStream.class), anyLong(), eq("image/png"), eq("rex.png")))
                .thenReturn(new StoredImage("file-key", "image/png", "rex.png", 100L));
        when(imageStorage.type()).thenReturn(StorageType.MINIO);

        animalPhotoService.replacePhoto(1L, file);

        PhotoMetadata metadata = animal.getPhotoMetadata();
        assertThat(metadata).isNotNull();
        assertThat(metadata.getPhotoFileId()).isEqualTo("file-key");
        assertThat(metadata.getStorageType()).isEqualTo(StorageType.MINIO);
        verify(imageStorage, never()).delete(any());
    }

    @Test
    void replacePhoto_ExistingPhoto_DeletesOldImageBeforeStoringNew() throws IOException {
        AnimalEntity animal = availableAnimal();
        animal.setPhotoMetadata(new PhotoMetadata("old-key", StorageType.MINIO, "image/png", "old.png", 10L));
        MultipartFile file = imageFile("image/png", "rex.png", 100L);
        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));
        when(imageStorage.save(any(InputStream.class), anyLong(), eq("image/png"), eq("rex.png")))
                .thenReturn(new StoredImage("new-key", "image/png", "rex.png", 100L));
        when(imageStorage.type()).thenReturn(StorageType.MINIO);

        animalPhotoService.replacePhoto(1L, file);

        verify(imageStorage).delete("old-key");
        assertThat(animal.getPhotoMetadata().getPhotoFileId()).isEqualTo("new-key");
    }

    @Test
    void replacePhoto_EmptyFile_ThrowsAnimalPhotoException() throws IOException {
        AnimalEntity animal = availableAnimal();
        MultipartFile file = imageFile("image/png", "rex.png", 0L);
        when(file.isEmpty()).thenReturn(true);
        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));

        assertThatThrownBy(() -> animalPhotoService.replacePhoto(1L, file))
                .isInstanceOf(AnimalPhotoException.class);
        verify(imageStorage, never()).save(any(), anyLong(), any(), any());
    }

    @Test
    void replacePhoto_NonImageContentType_ThrowsAnimalPhotoException() throws IOException {
        AnimalEntity animal = availableAnimal();
        MultipartFile file = imageFile("application/pdf", "rex.pdf", 100L);
        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));

        assertThatThrownBy(() -> animalPhotoService.replacePhoto(1L, file))
                .isInstanceOf(AnimalPhotoException.class);
        verify(imageStorage, never()).save(any(), anyLong(), any(), any());
    }

    @Test
    void replacePhoto_InputStreamFailure_ThrowsAnimalPhotoException() throws IOException {
        AnimalEntity animal = availableAnimal();
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));
        when(file.getInputStream()).thenThrow(new IOException("boom"));

        assertThatThrownBy(() -> animalPhotoService.replacePhoto(1L, file))
                .isInstanceOf(AnimalPhotoException.class);
    }

    @Test
    void replacePhoto_UnknownAnimal_ThrowsAnimalNotFoundException() {
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(animalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> animalPhotoService.replacePhoto(99L, file))
                .isInstanceOf(AnimalNotFoundException.class);
    }

    @Test
    void download_AnimalWithoutPhoto_ThrowsAnimalPhotoNotFoundException() {
        AnimalEntity animal = availableAnimal();
        animal.setPhotoMetadata(null);
        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));

        assertThatThrownBy(() -> animalPhotoService.download(1L))
                .isInstanceOf(AnimalPhotoNotFoundException.class);
    }

    @Test
    void download_AnimalWithPhoto_ReturnsStoredImageStream() {
        AnimalEntity animal = availableAnimal();
        animal.setPhotoMetadata(new PhotoMetadata("file-key", StorageType.MINIO, "image/png", "rex.png", 10L));
        StoredImageStream stream = new StoredImageStream(
                new ByteArrayInputStream(new byte[]{1, 2, 3}), "image/png", "rex.png", 3L);
        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));
        when(imageStorage.load("file-key")).thenReturn(stream);

        assertThat(animalPhotoService.download(1L)).isEqualTo(stream);
    }

    @Test
    void delete_AnimalWithPhoto_DeletesImageAndClearsMetadata() {
        AnimalEntity animal = availableAnimal();
        animal.setPhotoMetadata(new PhotoMetadata("file-key", StorageType.MINIO, "image/png", "rex.png", 10L));
        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));

        animalPhotoService.delete(1L);

        verify(imageStorage).delete("file-key");
        assertThat(animal.getPhotoMetadata()).isNull();
    }

    @Test
    void delete_AnimalWithoutPhoto_ThrowsAnimalPhotoNotFoundException() {
        AnimalEntity animal = availableAnimal();
        animal.setPhotoMetadata(null);
        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));

        assertThatThrownBy(() -> animalPhotoService.delete(1L))
                .isInstanceOf(AnimalPhotoNotFoundException.class);
        verify(imageStorage, never()).delete(any());
    }

    private MultipartFile imageFile(String contentType, String filename, long size) throws IOException {
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        lenient().when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{1, 2, 3}));
        lenient().when(file.getContentType()).thenReturn(contentType);
        lenient().when(file.getOriginalFilename()).thenReturn(filename);
        lenient().when(file.getSize()).thenReturn(size);
        return file;
    }
}
