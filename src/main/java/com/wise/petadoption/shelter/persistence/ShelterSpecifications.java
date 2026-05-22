package com.wise.petadoption.shelter.persistence;

import org.springframework.data.jpa.domain.Specification;

public final class ShelterSpecifications {

    private ShelterSpecifications() {
    }

    public static Specification<ShelterEntity> cityEquals(String city) {

        return (root, query, cb) ->
                city == null
                        ? null
                        : cb.equal(root.get("city"), city);
    }

    public static Specification<ShelterEntity> verifiedEquals(Boolean verified) {

        return (root, query, cb) ->
                verified == null
                        ? null
                        : cb.equal(root.get("verified"), verified);
    }
}
