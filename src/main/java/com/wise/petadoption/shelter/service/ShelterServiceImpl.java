package com.wise.petadoption.shelter.service;

import com.wise.petadoption.shelter.domain.ModifyShelterCommand;
import com.wise.petadoption.shelter.domain.Shelter;
import com.wise.petadoption.shelter.exception.ShelterNotFoundException;
import com.wise.petadoption.shelter.mapper.ShelterMapper;
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
    private final ShelterMapper shelterMapper;
    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public Page<Shelter> getAll(String city, Boolean verified, Pageable pageable) {
        Specification<ShelterEntity> specification = Specification
                .where(ShelterSpecifications.cityEquals(city))
                .and(ShelterSpecifications.verifiedEquals(verified));

        return shelterRepository.findAll(specification, pageable).map(shelterMapper::toModel);
    }

    @Override
    @Transactional(readOnly = true)
    public Shelter getById(long id) {
        ShelterEntity shelterEntity = getEntityById(id);
        return shelterMapper.toModel(shelterEntity);
    }

    @Override
    @Transactional
    public Shelter create(ModifyShelterCommand command) {
        ShelterEntity shelterEntity = shelterMapper.toEntity(command);
        ShelterEntity saved = shelterRepository.save(shelterEntity);
        entityManager.flush();
        entityManager.refresh(saved);
        return shelterMapper.toModel(saved);
    }

    @Override
    @Transactional
    public Shelter update(ModifyShelterCommand command) {
        ShelterEntity loadedEntity = getEntityById(command.id());

        loadedEntity.setName(command.name());
        loadedEntity.setCity(command.city());
        loadedEntity.setAddress(command.address());
        loadedEntity.setContactEmail(command.contactEmail());
        loadedEntity.setContactPhone(command.contactPhone());
        loadedEntity.setDescription(command.description());

        return shelterMapper.toModel(loadedEntity);
    }

    @Override
    @Transactional
    public Shelter verify(Long id) {
        ShelterEntity loadedEntity = getEntityById(id);

        loadedEntity.setVerified(true);

        return shelterMapper.toModel(loadedEntity);
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
}
