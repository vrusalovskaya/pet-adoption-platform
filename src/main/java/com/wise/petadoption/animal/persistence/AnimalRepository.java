package com.wise.petadoption.animal.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface AnimalRepository extends
        JpaRepository<AnimalEntity, Long>,
        JpaSpecificationExecutor<AnimalEntity> {

    @Modifying
    @Query("""
                update AnimalEntity a
                set a.status = 'RESERVED'
                where a.id = :id
                and a.status = 'AVAILABLE'
            """)
    int reserveAnimal(Long id);
}
