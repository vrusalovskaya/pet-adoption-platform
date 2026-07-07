package com.wise.petadoption;

import com.wise.petadoption.adoption.common.ApplicationStatus;
import com.wise.petadoption.adoption.domain.Application;
import com.wise.petadoption.adoption.domain.CreateApplicationCommand;
import com.wise.petadoption.adoption.service.ApplicationService;
import com.wise.petadoption.animal.common.AnimalStatus;
import com.wise.petadoption.animal.common.Gender;
import com.wise.petadoption.animal.common.Species;
import com.wise.petadoption.animal.domain.Animal;
import com.wise.petadoption.animal.domain.CreateAnimalCommand;
import com.wise.petadoption.animal.service.AnimalService;
import com.wise.petadoption.shelter.domain.CreateShelterCommand;
import com.wise.petadoption.shelter.domain.Shelter;
import com.wise.petadoption.shelter.service.ShelterService;
import com.wise.petadoption.support.AbstractFullStackIT;
import com.wise.petadoption.user.common.Role;
import com.wise.petadoption.user.domain.CreateUserCommand;
import com.wise.petadoption.user.domain.User;
import com.wise.petadoption.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ServiceLayerIT extends AbstractFullStackIT {

    @Autowired
    private UserService userService;
    @Autowired
    private ShelterService shelterService;
    @Autowired
    private AnimalService animalService;
    @Autowired
    private ApplicationService applicationService;

    @Test
    void create_NewUser_PersistsAndIsRetrievableByEmail() {
        CreateUserCommand command = new CreateUserCommand("service-it@example.com", "raw-password",
                "Jane", "Doe", "+15551234567", Role.ROLE_USER);

        User created = userService.create(command);
        User found = userService.findByEmail("service-it@example.com");

        assertThat(created.id()).isNotNull();
        assertThat(created.passwordHash()).isNotEqualTo("raw-password");
        assertThat(found.id()).isEqualTo(created.id());
        assertThat(created.createdAt()).isNotNull();
    }

    @Test
    void create_NewShelter_PersistsWithGeneratedIdAndTimestamp() {
        Shelter created = shelterService.create(new CreateShelterCommand(
                "IT Shelter", "ITCity", "1 Main St",
                "it-shelter@example.com", "+15557654321", "desc"));

        assertThat(created.id()).isNotNull();
        assertThat(created.createdAt()).isNotNull();
        assertThat(shelterService.get(created.id()).name()).isEqualTo("IT Shelter");
    }

    @Test
    void create_NewAnimal_DefaultsToAvailableStatus() {
        Shelter shelter = shelterService.create(new CreateShelterCommand(
                "Animal Shelter", "ITCity", "1 Main St",
                "animal-shelter@example.com", "+15557654321", "desc"));

        Animal created = animalService.create(new CreateAnimalCommand(
                shelter.id(), "Rex", Species.DOG, "Labrador", 2020, Gender.MALE, "Good boy"));

        assertThat(created.id()).isNotNull();
        assertThat(created.status()).isEqualTo(AnimalStatus.AVAILABLE);
        assertThat(created.shelterId()).isEqualTo(shelter.id());
    }

    @Test
    void approve_PendingApplication_ApprovesAndReservesAnimal() {
        Shelter shelter = shelterService.create(new CreateShelterCommand(
                "Approve Shelter", "ITCity", "1 Main St",
                "approve-shelter@example.com", "+15557654321", "desc"));
        Animal animal = animalService.create(new CreateAnimalCommand(
                shelter.id(), "Buddy", Species.DOG, "Beagle", 2021, Gender.MALE, "Friendly"));
        Application application = applicationService.create(
                new CreateApplicationCommand(animal.id(), 1L, "I would love to adopt Buddy"));

        Application approved = applicationService.approve(application.id());

        assertThat(approved.status()).isEqualTo(ApplicationStatus.APPROVED);
        assertThat(animalService.get(animal.id()).status()).isEqualTo(AnimalStatus.RESERVED);
    }
}
