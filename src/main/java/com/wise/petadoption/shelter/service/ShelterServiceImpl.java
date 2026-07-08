package com.wise.petadoption.shelter.service;

import com.wise.petadoption.shelter.domain.CreateShelterCommand;
import com.wise.petadoption.shelter.domain.UpdateShelterCommand;
import com.wise.petadoption.shelter.domain.Shelter;
import com.wise.petadoption.shelter.exception.ShelterNotFoundException;
import com.wise.petadoption.shelter.mapper.ShelterEntityMapper;
import com.wise.petadoption.shelter.persistence.ShelterEntity;
import com.wise.petadoption.shelter.persistence.ShelterRepository;
import com.wise.petadoption.shelter.persistence.ShelterSpecifications;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShelterServiceImpl implements ShelterService {
    private final ShelterRepository shelterRepository;
    private final ShelterEntityMapper entityMapper;
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public Page<Shelter> getAll(String city, Boolean verified, Pageable pageable) {
        Specification<ShelterEntity> specification = buildSpecification(city, verified);
        return shelterRepository.findAll(specification, pageable).map(entityMapper::toModel);
    }

    @Override
    @Transactional(readOnly = true)
    public Shelter get(long id) {
        ShelterEntity shelterEntity = getEntityById(id);
        return entityMapper.toModel(shelterEntity);
    }

    @Override
    @Transactional
    public Shelter create(CreateShelterCommand command) {
        ShelterEntity shelterEntity = entityMapper.toEntity(command);
        ShelterEntity saved = saveAndRefresh(shelterEntity);
        return entityMapper.toModel(saved);
    }

    @Override
    @Transactional
    public Shelter update(UpdateShelterCommand command) {
        ShelterEntity loadedEntity = getEntityById(command.id());
        entityMapper.updateEntity(command, loadedEntity);
        return entityMapper.toModel(loadedEntity);
    }

    @Override
    @Transactional
    public Shelter verify(Long id) {
        ShelterEntity loadedEntity = getEntityById(id);
        loadedEntity.setVerified(true);
        return entityMapper.toModel(loadedEntity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ShelterEntity entity = getEntityById(id);
        shelterRepository.delete(entity);
    }

    private ShelterEntity getEntityById(Long id) {
        return shelterRepository.findById(id).orElseThrow(() -> new ShelterNotFoundException(id));
    }

    private Specification<ShelterEntity> buildSpecification(String city, Boolean verified) {
        return Specification
                .where(ShelterSpecifications.cityEquals(city))
                .and(ShelterSpecifications.verifiedEquals(verified));
    }

    private ShelterEntity saveAndRefresh(ShelterEntity shelterEntity) {
        ShelterEntity saved = shelterRepository.save(shelterEntity);
        entityManager.flush();
        entityManager.refresh(saved);
        return saved;
    }
}
