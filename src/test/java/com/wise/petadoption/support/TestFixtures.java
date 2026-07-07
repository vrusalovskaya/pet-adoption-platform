package com.wise.petadoption.support;

import com.wise.petadoption.adoption.common.ApplicationStatus;
import com.wise.petadoption.adoption.domain.Application;
import com.wise.petadoption.adoption.persistence.ApplicationEntity;
import com.wise.petadoption.animal.common.AnimalStatus;
import com.wise.petadoption.animal.common.Gender;
import com.wise.petadoption.animal.common.Species;
import com.wise.petadoption.animal.domain.Animal;
import com.wise.petadoption.animal.domain.CreateAnimalCommand;
import com.wise.petadoption.animal.domain.UpdateAnimalCommand;
import com.wise.petadoption.animal.persistence.AnimalEntity;
import com.wise.petadoption.security.domain.SecurityUser;
import com.wise.petadoption.shelter.domain.CreateShelterCommand;
import com.wise.petadoption.shelter.domain.UpdateShelterCommand;
import com.wise.petadoption.shelter.domain.Shelter;
import com.wise.petadoption.shelter.persistence.ShelterEntity;
import com.wise.petadoption.user.common.Role;
import com.wise.petadoption.user.domain.User;
import com.wise.petadoption.user.persistence.UserEntity;

import java.time.Instant;

public final class TestFixtures {

    public static final Instant NOW = Instant.parse("2026-01-01T12:00:00.00Z");

    private TestFixtures() {
    }

    public static UserEntity userEntity(Long id, String email, Role role) {
        UserEntity entity = new UserEntity();
        entity.setId(id);
        entity.setEmail(email);
        entity.setPasswordHash("hashed-secret");
        entity.setFirstName("Jane");
        entity.setLastName("Doe");
        entity.setPhone("+15551234567");
        entity.setRole(role);
        entity.setCreatedAt(NOW);
        return entity;
    }

    public static User user(Long id, String email, Role role) {
        return new User(id, email, "hashed-secret", "Jane", "Doe", "+15551234567", role, NOW);
    }

    public static SecurityUser securityUser(Long id, String email, Role role) {
        return new SecurityUser(id, email, "hashed-secret", "Jane", "Doe", role);
    }

    public static ShelterEntity shelterEntity(Long id) {
        ShelterEntity entity = new ShelterEntity();
        entity.setId(id);
        entity.setName("Happy Tails");
        entity.setCity("Springfield");
        entity.setAddress("1 Main St");
        entity.setContactEmail("shelter@example.com");
        entity.setContactPhone("+15557654321");
        entity.setDescription("A cozy shelter");
        entity.setVerified(false);
        entity.setCreatedAt(NOW);
        return entity;
    }

    public static Shelter shelter(Long id) {
        return new Shelter(id, "Happy Tails", "Springfield", "1 Main St",
                "shelter@example.com", "+15557654321", "A cozy shelter", false, NOW);
    }

    public static CreateShelterCommand createShelterCommand() {
        return new CreateShelterCommand("Happy Tails", "Springfield", "1 Main St",
                "shelter@example.com", "+15557654321", "A cozy shelter");
    }

    public static UpdateShelterCommand updateShelterCommand(Long id) {
        return new UpdateShelterCommand(id, "Happy Tails", "Springfield", "1 Main St",
                "shelter@example.com", "+15557654321", "A cozy shelter");
    }

    public static AnimalEntity animalEntity(Long id, AnimalStatus status, ShelterEntity shelter) {
        AnimalEntity entity = new AnimalEntity();
        entity.setId(id);
        entity.setShelterEntity(shelter);
        entity.setName("Rex");
        entity.setSpecies(Species.DOG);
        entity.setBreed("Labrador");
        entity.setBirthYear(2020);
        entity.setGender(Gender.MALE);
        entity.setDescription("Good boy");
        entity.setStatus(status);
        entity.setCreatedAt(NOW);
        return entity;
    }

    public static Animal animal(Long id, AnimalStatus status, Long shelterId) {
        return new Animal(id, shelterId, "Rex", Species.DOG, "Labrador", 2020,
                Gender.MALE, "Good boy", status, null, NOW);
    }

    public static CreateAnimalCommand createAnimalCommand(Long shelterId) {
        return new CreateAnimalCommand(shelterId, "Rex", Species.DOG, "Labrador",
                2020, Gender.MALE, "Good boy");
    }

    public static UpdateAnimalCommand updateAnimalCommand(Long id, Long shelterId) {
        return new UpdateAnimalCommand(id, shelterId, "Rex", Species.DOG, "Labrador",
                2020, Gender.MALE, "Good boy");
    }

    public static ApplicationEntity applicationEntity(Long id, Long animalId, Long applicantId,
                                                      ApplicationStatus status) {
        ApplicationEntity entity = new ApplicationEntity();
        entity.setId(id);
        entity.setAnimalId(animalId);
        entity.setApplicantId(applicantId);
        entity.setMessage("Please let me adopt");
        entity.setStatus(status);
        entity.setCreatedAt(NOW);
        entity.setUpdatedAt(NOW);
        return entity;
    }

    public static Application application(Long id, Long animalId, Long applicantId,
                                          ApplicationStatus status) {
        return new Application(id, animalId, applicantId, "Please let me adopt", status,
                null, NOW, NOW);
    }
}
