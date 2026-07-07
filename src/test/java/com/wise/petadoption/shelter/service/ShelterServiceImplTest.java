package com.wise.petadoption.shelter.service;

import com.wise.petadoption.shelter.domain.UpdateShelterCommand;
import com.wise.petadoption.shelter.exception.ShelterNotFoundException;
import com.wise.petadoption.shelter.mapper.ShelterEntityMapper;
import com.wise.petadoption.shelter.persistence.ShelterEntity;
import com.wise.petadoption.shelter.persistence.ShelterRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.wise.petadoption.support.TestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShelterServiceImplTest {

    @Mock
    private ShelterRepository shelterRepository;
    @Mock
    private ShelterEntityMapper entityMapper;
    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private ShelterServiceImpl shelterService;

    @Test
    void get_UnknownId_ThrowsShelterNotFoundException() {
        when(shelterRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shelterService.get(99L))
                .isInstanceOf(ShelterNotFoundException.class);
    }

    @Test
    void update_ExistingShelter_UpdatesMutableFields() {
        ShelterEntity entity = shelterEntity(1L);
        UpdateShelterCommand command = new UpdateShelterCommand(1L, "New Name", "New City",
                "New Address", "new@example.com", "+15559998877", "New description");
        when(shelterRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(entityMapper.toModel(entity)).thenReturn(shelter(1L));

        shelterService.update(command);

        assertThat(entity.getName()).isEqualTo("New Name");
        assertThat(entity.getCity()).isEqualTo("New City");
        assertThat(entity.getContactEmail()).isEqualTo("new@example.com");
    }

    @Test
    void update_UnknownShelter_ThrowsShelterNotFoundException() {
        UpdateShelterCommand command = updateShelterCommand(99L);
        when(shelterRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shelterService.update(command))
                .isInstanceOf(ShelterNotFoundException.class);
    }

    @Test
    void verify_ExistingShelter_MarksShelterVerified() {
        ShelterEntity entity = shelterEntity(1L);
        entity.setVerified(false);
        when(shelterRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(entityMapper.toModel(entity)).thenReturn(shelter(1L));

        shelterService.verify(1L);

        assertThat(entity.isVerified()).isTrue();
    }

    @Test
    void delete_ExistingShelter_DeletesEntity() {
        ShelterEntity entity = shelterEntity(1L);
        when(shelterRepository.findById(1L)).thenReturn(Optional.of(entity));

        shelterService.delete(1L);

        verify(shelterRepository).delete(entity);
    }

    @Test
    void delete_UnknownShelter_ThrowsShelterNotFoundException() {
        when(shelterRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shelterService.delete(99L))
                .isInstanceOf(ShelterNotFoundException.class);
        verify(shelterRepository, never()).delete(any(ShelterEntity.class));
    }
}
