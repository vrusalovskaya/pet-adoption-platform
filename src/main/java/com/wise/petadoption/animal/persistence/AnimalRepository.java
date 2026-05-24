package com.wise.petadoption.animal.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AnimalRepository extends
        JpaRepository<AnimalEntity, Long>,
        JpaSpecificationExecutor<AnimalEntity> {
}
