package com.wise.petadoption.shelter.service;

import com.wise.petadoption.shelter.domain.CreateShelterCommand;
import com.wise.petadoption.shelter.domain.UpdateShelterCommand;
import com.wise.petadoption.shelter.domain.Shelter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ShelterService {
    Page<Shelter> getAll(String city, Boolean verified, Pageable pageable);

    Shelter get(long id);

    Shelter create(CreateShelterCommand shelter);

    Shelter update(UpdateShelterCommand shelter);

    Shelter verify(Long id);

    void delete(Long id);
}
