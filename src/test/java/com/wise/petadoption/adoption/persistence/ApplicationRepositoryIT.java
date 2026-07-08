package com.wise.petadoption.adoption.persistence;

import com.wise.petadoption.adoption.common.ApplicationStatus;
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
class ApplicationRepositoryIT extends AbstractPostgresIT {

    private static final long APPLICANT_ID = 987_654L;
    private static final long OTHER_APPLICANT_ID = 123_456L;

    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private TestEntityManager entityManager;

    private ApplicationEntity persistApplication(long applicantId, long animalId, ApplicationStatus status) {
        ApplicationEntity application = new ApplicationEntity();
        application.setApplicantId(applicantId);
        application.setAnimalId(animalId);
        application.setMessage("Please let me adopt");
        application.setStatus(status);
        return entityManager.persistAndFlush(application);
    }

    @Test
    void findByApplicantId_ApplicantHasApplications_ReturnsOnlyTheirApplications() {
        persistApplication(APPLICANT_ID, 1L, ApplicationStatus.PENDING);
        persistApplication(APPLICANT_ID, 2L, ApplicationStatus.APPROVED);
        persistApplication(OTHER_APPLICANT_ID, 3L, ApplicationStatus.PENDING);

        Page<ApplicationEntity> result =
                applicationRepository.findByApplicantId(APPLICANT_ID, PageRequest.of(0, 10));

        assertThat(result.getContent())
                .hasSize(2)
                .allMatch(application -> application.getApplicantId() == APPLICANT_ID);
    }

    @Test
    void findAll_StatusAndApplicantSpecification_ReturnsMatchingApplications() {
        persistApplication(APPLICANT_ID, 10L, ApplicationStatus.PENDING);
        persistApplication(APPLICANT_ID, 11L, ApplicationStatus.REJECTED);

        Page<ApplicationEntity> result = applicationRepository.findAll(
                Specification.where(ApplicationSpecifications.applicantIdEquals(APPLICANT_ID))
                        .and(ApplicationSpecifications.statusEquals(ApplicationStatus.PENDING)),
                PageRequest.of(0, 10));

        assertThat(result.getContent())
                .isNotEmpty()
                .allMatch(application -> application.getStatus() == ApplicationStatus.PENDING
                        && application.getApplicantId() == APPLICANT_ID);
    }

    @Test
    void findAll_AnimalIdSpecification_ReturnsMatchingApplications() {
        persistApplication(APPLICANT_ID, 555L, ApplicationStatus.PENDING);

        Page<ApplicationEntity> result = applicationRepository.findAll(
                Specification.where(ApplicationSpecifications.animalIdEquals(555L)),
                PageRequest.of(0, 10));

        assertThat(result.getContent())
                .isNotEmpty()
                .allMatch(application -> application.getAnimalId() == 555L);
    }
}
