package com.wise.petadoption.shelter.persistence;

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
class ShelterRepositoryIT extends AbstractPostgresIT {

    @Autowired
    private ShelterRepository shelterRepository;
    @Autowired
    private TestEntityManager entityManager;

    private ShelterEntity newShelter(String city, boolean verified) {
        ShelterEntity shelter = new ShelterEntity();
        shelter.setName("Shelter " + city);
        shelter.setCity(city);
        shelter.setAddress("1 Main St");
        shelter.setContactEmail("shelter@example.com");
        shelter.setContactPhone("+15557654321");
        shelter.setDescription("desc");
        shelter.setVerified(verified);
        return shelter;
    }

    @Test
    void findAll_CitySpecification_ReturnsOnlyMatchingShelters() {
        entityManager.persistAndFlush(newShelter("Testopolis", true));
        entityManager.persistAndFlush(newShelter("Testopolis", false));
        entityManager.persistAndFlush(newShelter("Otherville", true));

        Page<ShelterEntity> result = shelterRepository.findAll(
                Specification.where(ShelterSpecifications.cityEquals("Testopolis")),
                PageRequest.of(0, 10));

        assertThat(result.getContent())
                .hasSize(2)
                .allMatch(shelter -> shelter.getCity().equals("Testopolis"));
    }

    @Test
    void findAll_VerifiedSpecification_ReturnsOnlyVerifiedShelters() {
        entityManager.persistAndFlush(newShelter("Verifytown", true));
        entityManager.persistAndFlush(newShelter("Verifytown", false));

        Page<ShelterEntity> result = shelterRepository.findAll(
                Specification.where(ShelterSpecifications.cityEquals("Verifytown"))
                        .and(ShelterSpecifications.verifiedEquals(true)),
                PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().isVerified()).isTrue();
    }
}
