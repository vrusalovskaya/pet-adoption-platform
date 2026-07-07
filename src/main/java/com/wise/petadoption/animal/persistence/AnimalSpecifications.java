package com.wise.petadoption.animal.persistence;

import com.wise.petadoption.animal.common.AnimalStatus;
import com.wise.petadoption.animal.common.Species;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AnimalSpecifications {

    public static Specification<AnimalEntity> speciesEquals(Species species) {
        return (root, query, cb) ->
                species == null ? cb.conjunction() : cb.equal(root.get("species"), species);
    }

    public static Specification<AnimalEntity> statusEquals(AnimalStatus status) {
        return (root, query, cb) ->
                status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
    }

    public static Specification<AnimalEntity> shelterIdEquals(Long shelterId) {

        return (root, query, cb) ->
                shelterId == null ? cb.conjunction() : cb.equal(root.get("shelterEntity").get("id"), shelterId);
    }
}
