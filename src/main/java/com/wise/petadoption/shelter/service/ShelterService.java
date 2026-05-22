package com.wise.petadoption.shelter.service;

import com.wise.petadoption.shelter.domain.ModifyShelterCommand;
import com.wise.petadoption.shelter.domain.Shelter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ShelterService {
    Page<Shelter> getAll(String city, Boolean verified, Pageable pageable);

    Shelter getById(long id);

    Shelter create(ModifyShelterCommand shelter);

    Shelter update(ModifyShelterCommand shelter);

    Shelter verify(Long id);

    void delete(Long id);
}
