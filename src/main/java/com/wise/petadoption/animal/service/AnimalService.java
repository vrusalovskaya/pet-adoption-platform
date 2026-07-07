package com.wise.petadoption.animal.service;

import com.wise.petadoption.animal.common.AnimalStatus;
import com.wise.petadoption.animal.common.Species;
import com.wise.petadoption.animal.domain.Animal;
import com.wise.petadoption.animal.domain.CreateAnimalCommand;
import com.wise.petadoption.animal.domain.UpdateAnimalCommand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AnimalService {
    Animal get(Long id);

    Page<Animal> getAll(Species species, AnimalStatus status, Long shelterId, Pageable pageable);

    Animal create(CreateAnimalCommand command);

    Animal update(UpdateAnimalCommand command);

    Animal setStatus(Long id, AnimalStatus status);

    void delete(Long id);

    void reserveIfAvailable(Long id);
}
