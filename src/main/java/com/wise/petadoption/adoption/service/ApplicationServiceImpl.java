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
import com.wise.petadoption.adoption.persistence.ApplicationSpecifications;
import com.wise.petadoption.animal.service.AnimalService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;


@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final AnimalService animalService;
    private final ApplicationEntityMapper entityMapper;
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public Application create(CreateApplicationCommand command) {
        ApplicationEntity applicationEntity = entityMapper.toEntity(command);
        applicationEntity.setStatus(ApplicationStatus.PENDING);
        ApplicationEntity saved = saveAndRefresh(applicationEntity);
        return entityMapper.toModel(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Application> getAllByApplicant(Long applicantId, Pageable pageable) {
        return applicationRepository.findByApplicantId(applicantId, pageable).map(entityMapper::toModel);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Application> getAll(ApplicationStatus status, Long animalId, Long applicantId,
                                    Pageable pageable) {
        Specification<ApplicationEntity> specification = buildSpecification(
                status,
                animalId,
                applicantId);

        return applicationRepository.findAll(specification, pageable).map(entityMapper::toModel);
    }

    @Override
    @Transactional(readOnly = true)
    public Application getForAdmin(Long id) {
        ApplicationEntity applicationEntity = getEntityById(id);
        return entityMapper.toModel(applicationEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Application getForUser(Long id, Long userId) {
        ApplicationEntity applicationEntity = getEntityById(id);
        validateOwnership(applicationEntity, userId);
        return entityMapper.toModel(applicationEntity);
    }

    @Override
    @Transactional
    public Application approve(Long id) {
        ApplicationEntity applicationEntity = getEntityById(id);
        validateStatusIsPending(applicationEntity);

        animalService.reserveIfAvailable(applicationEntity.getAnimalId());
        applicationEntity.setStatus(ApplicationStatus.APPROVED);
        saveAndRefresh(applicationEntity);

        return entityMapper.toModel(applicationEntity);
    }

    @Override
    @Transactional
    public Application reject(RejectionCommand command) {
        ApplicationEntity applicationEntity = getEntityById(command.id());
        validateStatusIsPending(applicationEntity);

        applicationEntity.setStatus(ApplicationStatus.REJECTED);
        applicationEntity.setDecisionComment(command.decisionComment());
        saveAndRefresh(applicationEntity);

        return entityMapper.toModel(applicationEntity);
    }

    @Override
    @Transactional
    public Application cancel(Long id, Long userId) {
        ApplicationEntity applicationEntity = getEntityById(id);
        validateOwnership(applicationEntity, userId);
        validateStatusIsPending(applicationEntity);

        applicationEntity.setStatus(ApplicationStatus.CANCELLED);
        saveAndRefresh(applicationEntity);

        return entityMapper.toModel(applicationEntity);
    }

    private ApplicationEntity saveAndRefresh(ApplicationEntity applicationEntity) {
        ApplicationEntity saved = applicationRepository.save(applicationEntity);

        entityManager.flush();
        entityManager.refresh(saved);

        return saved;
    }

    private Specification<ApplicationEntity> buildSpecification(ApplicationStatus status, Long animalId, Long applicantId) {
        return Specification
                .where(ApplicationSpecifications.statusEquals(status))
                .and(ApplicationSpecifications.animalIdEquals(animalId))
                .and(ApplicationSpecifications.applicantIdEquals(applicantId));
    }

    private ApplicationEntity getEntityById(Long id) {
        return applicationRepository.findById(id).orElseThrow(() -> new ApplicationNotFoundException(id));
    }

    private void validateStatusIsPending(ApplicationEntity applicationEntity) {
        if (applicationEntity.getStatus() != ApplicationStatus.PENDING) {
            throw new ApplicationNotPendingException(applicationEntity.getStatus());
        }
    }

    private void validateOwnership(ApplicationEntity applicationEntity, Long userId) {
        if (!Objects.equals(applicationEntity.getApplicantId(), userId)) {
            throw new ApplicationAccessDeniedException();
        }
    }
}
