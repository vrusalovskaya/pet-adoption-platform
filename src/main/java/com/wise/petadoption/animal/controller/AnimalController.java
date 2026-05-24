package com.wise.petadoption.animal.controller;

import com.wise.petadoption.animal.api.AnimalRequest;
import com.wise.petadoption.animal.api.AnimalResponse;
import com.wise.petadoption.animal.api.UpdateAnimalStatusRequest;
import com.wise.petadoption.animal.common.AnimalStatus;
import com.wise.petadoption.animal.common.Species;
import com.wise.petadoption.animal.domain.Animal;
import com.wise.petadoption.animal.domain.ModifyAnimalCommand;
import com.wise.petadoption.animal.mapper.AnimalResponseMapper;
import com.wise.petadoption.animal.service.AnimalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/animals")
@RequiredArgsConstructor
public class AnimalController {

    private final AnimalService animalService;
    private final AnimalResponseMapper responseMapper;

    @GetMapping("/{id}")
    public AnimalResponse get(@PathVariable Long id) {
        Animal animal = animalService.get(id);
        return responseMapper.toResponse(animal);
    }

    @GetMapping
    public Page<AnimalResponse> getAll(@RequestParam(required = false) Species species,
                                       @RequestParam(required = false) AnimalStatus status,
                                       @RequestParam(required = false) Long shelterId,
                                       @ParameterObject
                                       @PageableDefault(
                                               size = 20,
                                               sort = "createdAt",
                                               direction = Sort.Direction.DESC
                                       )
                                       Pageable pageable) {
        return animalService.getAll(species, status, shelterId, pageable).map(responseMapper::toResponse);
    }

    @PostMapping
    public ResponseEntity<AnimalResponse> create(@Valid @RequestBody AnimalRequest request) {
        ModifyAnimalCommand command = toCommand(null, request);

        Animal animal = animalService.create(command);
        AnimalResponse animalResponse = responseMapper.toResponse(animal);

        URI location = URI.create(String.format("/api/v1/animals/%d", animalResponse.id()));
        return ResponseEntity.created(location).body(animalResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AnimalResponse> update(@PathVariable Long id, @Valid @RequestBody AnimalRequest request) {
        ModifyAnimalCommand command = toCommand(id, request);

        Animal updatedAnimal = animalService.update(command);
        AnimalResponse animalResponse = responseMapper.toResponse(updatedAnimal);
        return ResponseEntity.ok(animalResponse);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AnimalResponse> updateStatus(@PathVariable Long id,
                                                       @Valid @RequestBody UpdateAnimalStatusRequest request) {
        Animal animal = animalService.setStatus(id, request.status());
        AnimalResponse animalResponse = responseMapper.toResponse(animal);
        return ResponseEntity.ok(animalResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        animalService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private static ModifyAnimalCommand toCommand(Long id, AnimalRequest request) {
        return new ModifyAnimalCommand(
                id, request.shelterId(), request.name(), request.species(), request.breed(), request.birthYear(),
                request.gender(), request.description());
    }
}
