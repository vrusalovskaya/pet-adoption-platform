package com.wise.petadoption.shelter.persistence;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShelterSpecifications {

    public static Specification<ShelterEntity> cityEquals(String city) {
        return (root, query, cb) ->
                city == null ? cb.conjunction() : cb.equal(root.get("city"), city);
    }

    public static Specification<ShelterEntity> verifiedEquals(Boolean verified) {
        return (root, query, cb) ->
                verified == null ? cb.conjunction() : cb.equal(root.get("verified"), verified);
    }
}
