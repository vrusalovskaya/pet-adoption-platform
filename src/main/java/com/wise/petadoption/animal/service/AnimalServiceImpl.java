package com.wise.petadoption.animal.service;

import com.wise.petadoption.animal.common.AnimalStatus;
import com.wise.petadoption.animal.common.Species;
import com.wise.petadoption.animal.domain.Animal;
import com.wise.petadoption.animal.domain.ModifyAnimalCommand;
import com.wise.petadoption.animal.exception.AnimalNotAvailableException;
import com.wise.petadoption.animal.exception.AnimalNotFoundException;
import com.wise.petadoption.animal.mapper.AnimalEntityMapper;
import com.wise.petadoption.animal.persistence.AnimalEntity;
import com.wise.petadoption.animal.persistence.AnimalRepository;
import com.wise.petadoption.animal.persistence.AnimalSpecifications;
import com.wise.petadoption.shelter.exception.ShelterNotFoundException;
import com.wise.petadoption.shelter.persistence.ShelterEntity;
import com.wise.petadoption.shelter.persistence.ShelterRepository;
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
public class AnimalServiceImpl implements AnimalService {

    private final AnimalRepository animalRepository;
    private final ShelterRepository shelterRepository;
    private final AnimalPhotoService animalPhotoService;
    private final AnimalEntityMapper entityMapper;
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public Animal get(Long id) {
        AnimalEntity animal = getEntityById(id);
        return entityMapper.toModel(animal);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Animal> getAll(Species species, AnimalStatus status, Long shelterId, Pageable pageable) {
        Specification<AnimalEntity> specification = Specification
                .where(AnimalSpecifications.speciesEquals(species))
                .and(AnimalSpecifications.statusEquals(status))
                .and(AnimalSpecifications.shelterIdEquals(shelterId));

        return animalRepository.findAll(specification, pageable).map(entityMapper::toModel);
    }

    @Override
    @Transactional
    public Animal create(ModifyAnimalCommand command) {
        AnimalEntity animalEntity = entityMapper.toEntity(command);
        ShelterEntity shelterEntity = getShelterEntityById(command.shelterId());
        animalEntity.setShelterEntity(shelterEntity);
        animalEntity.setStatus(AnimalStatus.AVAILABLE);
        AnimalEntity saved = animalRepository.save(animalEntity);
        entityManager.flush();
        entityManager.refresh(saved);
        return entityMapper.toModel(saved);
    }

    @Override
    @Transactional
    public Animal update(ModifyAnimalCommand command) {
        AnimalEntity loadedEntity = getEntityById(command.id());

        ShelterEntity shelterEntity = getShelterEntityById(command.shelterId());
        loadedEntity.setShelterEntity(shelterEntity);
        loadedEntity.setName(command.name());
        loadedEntity.setSpecies(command.species());
        loadedEntity.setBreed(command.breed());
        loadedEntity.setBirthYear(command.birthYear());
        loadedEntity.setGender(command.gender());
        loadedEntity.setDescription(command.description());

        return entityMapper.toModel(loadedEntity);
    }

    @Override
    @Transactional
    public Animal setStatus(Long id, AnimalStatus status) {
        AnimalEntity loadedEntity = getEntityById(id);
        loadedEntity.setStatus(status);
        return entityMapper.toModel(loadedEntity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        AnimalEntity loadedEntity = getEntityById(id);
        if (loadedEntity.getPhotoMetadata() != null) {
            animalPhotoService.delete(id);
        }
        animalRepository.delete(loadedEntity);
    }

    @Override
    public void reserveIfAvailable(Long id) {
        int updated = animalRepository.reserveAnimal(id);

        if (updated == 0) {
            throw new AnimalNotAvailableException(id);
        }
    }

    private AnimalEntity getEntityById(Long id) {
        return animalRepository.findById(id).orElseThrow(() -> new AnimalNotFoundException(id));
    }

    private ShelterEntity getShelterEntityById(Long id) {
        return shelterRepository.findById(id).orElseThrow(() -> new ShelterNotFoundException(id));
    }
}
