package com.wise.petadoption.animal.service;

import com.wise.petadoption.animal.common.AnimalStatus;
import com.wise.petadoption.animal.common.Species;
import com.wise.petadoption.animal.domain.Animal;
import com.wise.petadoption.animal.domain.CreateAnimalCommand;
import com.wise.petadoption.animal.domain.UpdateAnimalCommand;
import com.wise.petadoption.animal.exception.AnimalNotAvailableException;
import com.wise.petadoption.animal.exception.AnimalNotFoundException;
import com.wise.petadoption.animal.exception.NotValidAnimalStatusTransitionException;
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

import java.util.Map;
import java.util.Set;

import static com.wise.petadoption.animal.common.AnimalStatus.*;

@Service
@RequiredArgsConstructor
public class AnimalServiceImpl implements AnimalService {

    /**
     * Defines the valid lifecycle transitions for an animal.
     * <p>
     * AVAILABLE  -> RESERVED, WITHDRAWN
     * RESERVED   -> ADOPTED, WITHDRAWN
     * ADOPTED    -> (no further transitions)
     * WITHDRAWN  -> (no further transitions)
     */
    private static final Map<AnimalStatus, Set<AnimalStatus>> ALLOWED_TRANSITIONS =
            Map.of(
                    AVAILABLE, Set.of(RESERVED, WITHDRAWN),
                    RESERVED, Set.of(ADOPTED, WITHDRAWN),
                    ADOPTED, Set.of(),
                    WITHDRAWN, Set.of()
            );

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
        Specification<AnimalEntity> specification = buildSpecification(species, status, shelterId);
        return animalRepository.findAll(specification, pageable).map(entityMapper::toModel);
    }

    @Override
    @Transactional
    public Animal create(CreateAnimalCommand command) {
        AnimalEntity animalEntity = entityMapper.toEntity(command);

        ShelterEntity shelterEntity = getShelterEntityById(command.shelterId());
        animalEntity.setShelterEntity(shelterEntity);

        animalEntity.setStatus(AVAILABLE);

        AnimalEntity saved = saveAndRefresh(animalEntity);
        return entityMapper.toModel(saved);
    }

    @Override
    @Transactional
    public Animal update(UpdateAnimalCommand command) {
        AnimalEntity loadedEntity = getEntityById(command.id());
        ShelterEntity shelterEntity = getShelterEntityById(command.shelterId());

        entityMapper.updateEntity(command, loadedEntity);
        loadedEntity.setShelterEntity(shelterEntity);

        return entityMapper.toModel(loadedEntity);
    }

    @Override
    @Transactional
    public Animal setStatus(Long id, AnimalStatus status) {
        AnimalEntity loadedEntity = getEntityById(id);
        validateStatusTransition(loadedEntity.getStatus(), status);
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

    private Specification<AnimalEntity> buildSpecification(Species species,
                                                           AnimalStatus status,
                                                           Long shelterId) {
        return Specification
                .where(AnimalSpecifications.speciesEquals(species))
                .and(AnimalSpecifications.statusEquals(status))
                .and(AnimalSpecifications.shelterIdEquals(shelterId));
    }

    private AnimalEntity saveAndRefresh(AnimalEntity animalEntity) {
        AnimalEntity saved = animalRepository.save(animalEntity);
        entityManager.flush();
        entityManager.refresh(saved);
        return saved;
    }

    private void validateStatusTransition(AnimalStatus previousStatus, AnimalStatus newStatus) {
        if (!ALLOWED_TRANSITIONS.getOrDefault(previousStatus, Set.of()).contains(newStatus)) {
            throw new NotValidAnimalStatusTransitionException(previousStatus, newStatus);
        }
    }
}
