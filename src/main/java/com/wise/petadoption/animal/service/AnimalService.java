package com.wise.petadoption.animal.service;

import com.wise.petadoption.animal.common.AnimalStatus;
import com.wise.petadoption.animal.common.Species;
import com.wise.petadoption.animal.domain.Animal;
import com.wise.petadoption.animal.domain.ModifyAnimalCommand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AnimalService {
    Animal get(Long id);

    Page<Animal> getAll(Species species, AnimalStatus status, Long shelterId, Pageable pageable);

    Animal create(ModifyAnimalCommand command);

    Animal update(ModifyAnimalCommand command);

    Animal setStatus(Long id, AnimalStatus status);

    void delete(Long id);

    void reserveIfAvailable(Long id);
}
