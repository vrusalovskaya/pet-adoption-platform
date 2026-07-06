package com.wise.petadoption.adoption.service;

import com.wise.petadoption.adoption.common.ApplicationStatus;
import com.wise.petadoption.adoption.domain.Application;
import com.wise.petadoption.adoption.domain.CreateApplicationCommand;
import com.wise.petadoption.adoption.domain.RejectionCommand;
import com.wise.petadoption.adoption.exception.ApplicationAccessDeniedException;
import com.wise.petadoption.adoption.exception.ApplicationNotFoundException;
import com.wise.petadoption.adoption.exception.ApplicationNotPendingException;
import com.wise.petadoption.adoption.mapper.ApplicationEntityMapper;
import com.wise.petadoption.adoption.persistence.ApplicationEntity;
import com.wise.petadoption.adoption.persistence.ApplicationRepository;
import com.wise.petadoption.animal.service.AnimalService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static com.wise.petadoption.support.TestFixtures.application;
import static com.wise.petadoption.support.TestFixtures.applicationEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceImplTest {

    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private AnimalService animalService;
    @Mock
    private ApplicationEntityMapper entityMapper;
    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    @BeforeEach
    void injectEntityManager() {
        ReflectionTestUtils.setField(applicationService, "entityManager", entityManager);
    }

    @Test
    void create_ValidCommand_PersistsApplicationWithPendingStatus() {
        CreateApplicationCommand command = new CreateApplicationCommand(5L, 1L, "Please");
        ApplicationEntity saved = applicationEntity(10L, 5L, 1L, ApplicationStatus.PENDING);
        when(applicationRepository.save(any(ApplicationEntity.class))).thenReturn(saved);
        when(entityMapper.toModel(saved)).thenReturn(application(10L, 5L, 1L, ApplicationStatus.PENDING));

        applicationService.create(command);

        ArgumentCaptor<ApplicationEntity> captor = ArgumentCaptor.forClass(ApplicationEntity.class);
        verify(applicationRepository).save(captor.capture());
        ApplicationEntity persisted = captor.getValue();
        assertThat(persisted.getStatus()).isEqualTo(ApplicationStatus.PENDING);
        assertThat(persisted.getAnimalId()).isEqualTo(5L);
        assertThat(persisted.getApplicantId()).isEqualTo(1L);
        assertThat(persisted.getMessage()).isEqualTo("Please");
    }

    @Test
    void get_AdminUser_ReturnsApplicationWithoutOwnershipCheck() {
        ApplicationEntity entity = applicationEntity(10L, 5L, 999L, ApplicationStatus.PENDING);
        Application expected = application(10L, 5L, 999L, ApplicationStatus.PENDING);
        when(applicationRepository.findById(10L)).thenReturn(Optional.of(entity));
        when(entityMapper.toModel(entity)).thenReturn(expected);

        Application result = applicationService.getForAdmin(10L);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void get_OwnerUser_ReturnsApplication() {
        ApplicationEntity entity = applicationEntity(10L, 5L, 1L, ApplicationStatus.PENDING);
        Application expected = application(10L, 5L, 1L, ApplicationStatus.PENDING);
        when(applicationRepository.findById(10L)).thenReturn(Optional.of(entity));
        when(entityMapper.toModel(entity)).thenReturn(expected);

        Application result = applicationService.getForUser(10L, 1L);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void get_NonOwnerUser_ThrowsApplicationAccessDeniedException() {
        ApplicationEntity entity = applicationEntity(10L, 5L, 999L, ApplicationStatus.PENDING);
        when(applicationRepository.findById(10L)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> applicationService.getForUser(10L, 1L))
                .isInstanceOf(ApplicationAccessDeniedException.class);
    }

    @Test
    void get_UnknownApplication_ThrowsApplicationNotFoundException() {
        when(applicationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService.getForUser(99L, 1L))
                .isInstanceOf(ApplicationNotFoundException.class);
    }

    @Test
    void approve_PendingApplication_ReservesAnimalAndApproves() {
        ApplicationEntity entity = applicationEntity(10L, 5L, 1L, ApplicationStatus.PENDING);
        when(applicationRepository.findById(10L)).thenReturn(Optional.of(entity));
        when(entityMapper.toModel(entity)).thenReturn(application(10L, 5L, 1L, ApplicationStatus.APPROVED));

        applicationService.approve(10L);

        assertThat(entity.getStatus()).isEqualTo(ApplicationStatus.APPROVED);
        verify(animalService).reserveIfAvailable(5L);
    }

    @Test
    void approve_NonPendingApplication_ThrowsAndDoesNotReserve() {
        ApplicationEntity entity = applicationEntity(10L, 5L, 1L, ApplicationStatus.APPROVED);
        when(applicationRepository.findById(10L)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> applicationService.approve(10L))
                .isInstanceOf(ApplicationNotPendingException.class);
        verify(animalService, never()).reserveIfAvailable(anyLong());
    }

    @Test
    void reject_PendingApplication_SetsRejectedWithComment() {
        ApplicationEntity entity = applicationEntity(10L, 5L, 1L, ApplicationStatus.PENDING);
        RejectionCommand command = new RejectionCommand(10L, "Not a good fit");
        when(applicationRepository.findById(10L)).thenReturn(Optional.of(entity));
        when(entityMapper.toModel(entity)).thenReturn(application(10L, 5L, 1L, ApplicationStatus.REJECTED));

        applicationService.reject(command);

        assertThat(entity.getStatus()).isEqualTo(ApplicationStatus.REJECTED);
        assertThat(entity.getDecisionComment()).isEqualTo("Not a good fit");
    }

    @Test
    void reject_NonPendingApplication_ThrowsApplicationNotPendingException() {
        ApplicationEntity entity = applicationEntity(10L, 5L, 1L, ApplicationStatus.CANCELLED);
        when(applicationRepository.findById(10L)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> applicationService.reject(new RejectionCommand(10L, "x")))
                .isInstanceOf(ApplicationNotPendingException.class);
    }

    @Test
    void cancel_OwnerPendingApplication_SetsCancelled() {
        ApplicationEntity entity = applicationEntity(10L, 5L, 1L, ApplicationStatus.PENDING);
        when(applicationRepository.findById(10L)).thenReturn(Optional.of(entity));
        when(entityMapper.toModel(entity)).thenReturn(application(10L, 5L, 1L, ApplicationStatus.CANCELLED));

        applicationService.cancel(10L, 1L);

        assertThat(entity.getStatus()).isEqualTo(ApplicationStatus.CANCELLED);
    }

    @Test
    void cancel_NonOwner_ThrowsApplicationAccessDeniedException() {
        ApplicationEntity entity = applicationEntity(10L, 5L, 999L, ApplicationStatus.PENDING);
        when(applicationRepository.findById(10L)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> applicationService.cancel(10L, 1L))
                .isInstanceOf(ApplicationAccessDeniedException.class);
    }

    @Test
    void cancel_OwnerNonPendingApplication_ThrowsApplicationNotPendingException() {
        ApplicationEntity entity = applicationEntity(10L, 5L, 1L, ApplicationStatus.APPROVED);
        when(applicationRepository.findById(10L)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> applicationService.cancel(10L, 1L))
                .isInstanceOf(ApplicationNotPendingException.class);
    }
}
