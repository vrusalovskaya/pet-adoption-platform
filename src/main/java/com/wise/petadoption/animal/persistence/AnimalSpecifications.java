package com.wise.petadoption.animal.persistence;

import com.wise.petadoption.animal.common.AnimalStatus;
import com.wise.petadoption.animal.common.Species;
import org.springframework.data.jpa.domain.Specification;

public class AnimalSpecifications {

    private AnimalSpecifications() {
    }

    public static Specification<AnimalEntity> speciesEquals(Species species) {
        return (root, query, cb) ->
                species == null ? null : cb.equal(root.get("species"), species);
    }

    public static Specification<AnimalEntity> statusEquals(AnimalStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<AnimalEntity> shelterIdEquals(Long shelterId) {

        return (root, query, cb) ->
                shelterId == null ? null : cb.equal(root.get("shelterEntity").get("id"), shelterId);
    }
}
