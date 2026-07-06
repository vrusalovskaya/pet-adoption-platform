package com.wise.petadoption.animal.service;

import com.wise.petadoption.animal.common.AnimalStatus;
import com.wise.petadoption.animal.domain.ModifyAnimalCommand;
import com.wise.petadoption.animal.exception.AnimalNotAvailableException;
import com.wise.petadoption.animal.exception.AnimalNotFoundException;
import com.wise.petadoption.animal.exception.NotValidAnimalStatusTransitionException;
import com.wise.petadoption.animal.mapper.AnimalEntityMapper;
import com.wise.petadoption.animal.persistence.AnimalEntity;
import com.wise.petadoption.animal.persistence.AnimalRepository;
import com.wise.petadoption.animal.persistence.PhotoMetadata;
import com.wise.petadoption.shelter.exception.ShelterNotFoundException;
import com.wise.petadoption.shelter.persistence.ShelterEntity;
import com.wise.petadoption.shelter.persistence.ShelterRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static com.wise.petadoption.support.TestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnimalServiceImplTest {

    @Mock
    private AnimalRepository animalRepository;
    @Mock
    private ShelterRepository shelterRepository;
    @Mock
    private AnimalPhotoService animalPhotoService;
    @Mock
    private AnimalEntityMapper entityMapper;
    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private AnimalServiceImpl animalService;

    @BeforeEach
    void injectEntityManager() {
        ReflectionTestUtils.setField(animalService, "entityManager", entityManager);
    }

    @Test
    void create_ValidCommand_AttachesShelterAndSetsAvailableStatus() {
        ModifyAnimalCommand command = modifyAnimalCommand(null, 7L);
        AnimalEntity mapped = animalEntity(null, null, null);
        ShelterEntity shelter = shelterEntity(7L);
        AnimalEntity saved = animalEntity(1L, AnimalStatus.AVAILABLE, shelter);
        when(entityMapper.toEntity(command)).thenReturn(mapped);
        when(shelterRepository.findById(7L)).thenReturn(Optional.of(shelter));
        when(animalRepository.save(mapped)).thenReturn(saved);
        when(entityMapper.toModel(saved)).thenReturn(animal(1L, AnimalStatus.AVAILABLE, 7L));

        animalService.create(command);

        assertThat(mapped.getStatus()).isEqualTo(AnimalStatus.AVAILABLE);
        assertThat(mapped.getShelterEntity()).isEqualTo(shelter);
    }

    @Test
    void create_UnknownShelter_ThrowsShelterNotFoundException() {
        ModifyAnimalCommand command = modifyAnimalCommand(null, 7L);
        when(entityMapper.toEntity(command)).thenReturn(animalEntity(null, null, null));
        when(shelterRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> animalService.create(command))
                .isInstanceOf(ShelterNotFoundException.class);
        verify(animalRepository, never()).save(any());
    }

    @Test
    void update_UnknownAnimal_ThrowsAnimalNotFoundException() {
        when(animalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> animalService.update(modifyAnimalCommand(99L, 7L)))
                .isInstanceOf(AnimalNotFoundException.class);
    }

    @Test
    void update_UnknownShelter_ThrowsShelterNotFoundException() {
        AnimalEntity loaded = animalEntity(1L, AnimalStatus.AVAILABLE, shelterEntity(7L));
        when(animalRepository.findById(1L)).thenReturn(Optional.of(loaded));
        when(shelterRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> animalService.update(modifyAnimalCommand(1L, 7L)))
                .isInstanceOf(ShelterNotFoundException.class);
    }

    @Test
    void setStatus_AllowedTransition_UpdatesStatus() {
        AnimalEntity loaded = animalEntity(1L, AnimalStatus.AVAILABLE, shelterEntity(7L));
        when(animalRepository.findById(1L)).thenReturn(Optional.of(loaded));
        when(entityMapper.toModel(loaded)).thenReturn(animal(1L, AnimalStatus.RESERVED, 7L));

        animalService.setStatus(1L, AnimalStatus.RESERVED);

        assertThat(loaded.getStatus()).isEqualTo(AnimalStatus.RESERVED);
    }

    @Test
    void setStatus_ForbiddenTransition_ThrowsAndLeavesStatusUnchanged() {
        AnimalEntity loaded = animalEntity(1L, AnimalStatus.ADOPTED, shelterEntity(7L));
        when(animalRepository.findById(1L)).thenReturn(Optional.of(loaded));

        assertThatThrownBy(() -> animalService.setStatus(1L, AnimalStatus.AVAILABLE))
                .isInstanceOf(NotValidAnimalStatusTransitionException.class);
        assertThat(loaded.getStatus()).isEqualTo(AnimalStatus.ADOPTED);
    }

    @Test
    void delete_AnimalWithPhoto_DeletesPhotoThenAnimal() {
        AnimalEntity loaded = animalEntity(1L, AnimalStatus.AVAILABLE, shelterEntity(7L));
        loaded.setPhotoMetadata(new PhotoMetadata("file-1", null, "image/png", "rex.png", 10L));
        when(animalRepository.findById(1L)).thenReturn(Optional.of(loaded));

        animalService.delete(1L);

        verify(animalPhotoService).delete(1L);
        verify(animalRepository).delete(loaded);
    }

    @Test
    void delete_AnimalWithoutPhoto_DeletesAnimalOnly() {
        AnimalEntity loaded = animalEntity(1L, AnimalStatus.AVAILABLE, shelterEntity(7L));
        loaded.setPhotoMetadata(null);
        when(animalRepository.findById(1L)).thenReturn(Optional.of(loaded));

        animalService.delete(1L);

        verify(animalPhotoService, never()).delete(any());
        verify(animalRepository).delete(loaded);
    }

    @Test
    void reserveIfAvailable_AnimalNotAvailable_ThrowsAnimalNotAvailableException() {
        when(animalRepository.reserveAnimal(1L)).thenReturn(0);

        assertThatThrownBy(() -> animalService.reserveIfAvailable(1L))
                .isInstanceOf(AnimalNotAvailableException.class);
    }

    @Test
    void reserveIfAvailable_AnimalAvailable_DoesNotThrow() {
        when(animalRepository.reserveAnimal(1L)).thenReturn(1);

        animalService.reserveIfAvailable(1L);

        verify(animalRepository).reserveAnimal(1L);
    }
}
