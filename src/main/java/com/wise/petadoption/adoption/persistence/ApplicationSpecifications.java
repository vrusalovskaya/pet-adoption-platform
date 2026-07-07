package com.wise.petadoption.adoption.persistence;

import com.wise.petadoption.adoption.common.ApplicationStatus;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApplicationSpecifications {

    public static Specification<ApplicationEntity> statusEquals(ApplicationStatus status) {
        return (root, query, cb) ->
                status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<ApplicationEntity> animalIdEquals(Long animalId) {
        return (root, query, cb) ->
                animalId == null ? cb.conjunction() : cb.equal(root.get("animalId"), animalId);
    }

    public static Specification<ApplicationEntity> applicantIdEquals(Long applicantId) {
        return (root, query, cb) ->
                applicantId == null ? cb.conjunction() : cb.equal(root.get("applicantId"), applicantId);
    }
}
