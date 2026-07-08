package com.wise.petadoption.shelter.service;

import com.wise.petadoption.shelter.domain.UpdateShelterCommand;
import com.wise.petadoption.shelter.exception.ShelterNotFoundException;
import com.wise.petadoption.shelter.mapper.ShelterEntityMapper;
import com.wise.petadoption.shelter.mapper.ShelterEntityMapperImpl;
import com.wise.petadoption.shelter.persistence.ShelterEntity;
import com.wise.petadoption.shelter.persistence.ShelterRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static com.wise.petadoption.support.TestFixtures.shelterEntity;
import static com.wise.petadoption.support.TestFixtures.updateShelterCommand;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShelterServiceImplTest {

    private final ShelterEntityMapper entityMapper = new ShelterEntityMapperImpl();
    @Mock
    private ShelterRepository shelterRepository;
    @Mock
    private EntityManager entityManager;

    private ShelterServiceImpl shelterService;

    @BeforeEach
    void setUp() {
        shelterService = new ShelterServiceImpl(
                shelterRepository,
                entityMapper

        );

        ReflectionTestUtils.setField(shelterService, "entityManager", entityManager);
    }

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
