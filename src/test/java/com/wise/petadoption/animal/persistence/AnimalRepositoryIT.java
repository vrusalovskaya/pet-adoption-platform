package com.wise.petadoption.animal.persistence;

import com.wise.petadoption.animal.common.AnimalStatus;
import com.wise.petadoption.animal.common.Gender;
import com.wise.petadoption.animal.common.Species;
import com.wise.petadoption.shelter.persistence.ShelterEntity;
import com.wise.petadoption.support.AbstractPostgresIT;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AnimalRepositoryIT extends AbstractPostgresIT {

    @Autowired
    private AnimalRepository animalRepository;
    @Autowired
    private TestEntityManager entityManager;

    private ShelterEntity persistShelter() {
        ShelterEntity shelter = new ShelterEntity();
        shelter.setName("Shelter");
        shelter.setCity("AnimalCity");
        shelter.setContactEmail("shelter@example.com");
        shelter.setVerified(true);
        return entityManager.persistAndFlush(shelter);
    }

    private AnimalEntity persistAnimal(ShelterEntity shelter, Species species, AnimalStatus status) {
        AnimalEntity animal = new AnimalEntity();
        animal.setShelterEntity(shelter);
        animal.setName("Rex");
        animal.setSpecies(species);
        animal.setGender(Gender.MALE);
        animal.setStatus(status);
        return entityManager.persistAndFlush(animal);
    }

    @Test
    void reserveAnimal_AvailableAnimal_ReservesAndReturnsOne() {
        ShelterEntity shelter = persistShelter();
        AnimalEntity animal = persistAnimal(shelter, Species.DOG, AnimalStatus.AVAILABLE);

        int updated = animalRepository.reserveAnimal(animal.getId());
        entityManager.clear();

        assertThat(updated).isEqualTo(1);
        AnimalEntity reloaded = animalRepository.findById(animal.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(AnimalStatus.RESERVED);
    }

    @Test
    void reserveAnimal_NonAvailableAnimal_ReturnsZero() {
        ShelterEntity shelter = persistShelter();
        AnimalEntity animal = persistAnimal(shelter, Species.DOG, AnimalStatus.RESERVED);

        int updated = animalRepository.reserveAnimal(animal.getId());

        assertThat(updated).isZero();
    }

    @Test
    void findAll_SpeciesAndShelterSpecification_ReturnsMatchingAnimals() {
        ShelterEntity shelter = persistShelter();
        persistAnimal(shelter, Species.DOG, AnimalStatus.AVAILABLE);
        persistAnimal(shelter, Species.CAT, AnimalStatus.AVAILABLE);

        Page<AnimalEntity> result = animalRepository.findAll(
                Specification.where(AnimalSpecifications.speciesEquals(Species.DOG))
                        .and(AnimalSpecifications.shelterIdEquals(shelter.getId())),
                PageRequest.of(0, 10));

        assertThat(result.getContent())
                .isNotEmpty()
                .allMatch(animal -> animal.getSpecies() == Species.DOG);
    }

    @Test
    void findAll_StatusSpecification_ReturnsMatchingAnimals() {
        ShelterEntity shelter = persistShelter();
        persistAnimal(shelter, Species.DOG, AnimalStatus.ADOPTED);

        Page<AnimalEntity> result = animalRepository.findAll(
                Specification.where(AnimalSpecifications.statusEquals(AnimalStatus.ADOPTED))
                        .and(AnimalSpecifications.shelterIdEquals(shelter.getId())),
                PageRequest.of(0, 10));

        assertThat(result.getContent())
                .isNotEmpty()
                .allMatch(animal -> animal.getStatus() == AnimalStatus.ADOPTED);
    }
}
