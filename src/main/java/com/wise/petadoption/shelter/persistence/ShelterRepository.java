package com.wise.petadoption.shelter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ShelterRepository extends
        JpaRepository<ShelterEntity, Long>,
        JpaSpecificationExecutor<ShelterEntity> {
}
