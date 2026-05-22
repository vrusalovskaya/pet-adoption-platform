package com.wise.petadoption.shelter.controller;

import com.wise.petadoption.shelter.api.ShelterRequest;
import com.wise.petadoption.shelter.api.ShelterResponse;
import com.wise.petadoption.shelter.domain.ModifyShelterCommand;
import com.wise.petadoption.shelter.domain.Shelter;
import com.wise.petadoption.shelter.mapper.ShelterMapper;
import com.wise.petadoption.shelter.service.ShelterService;
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
@RequestMapping("/api/v1/shelters")
@RequiredArgsConstructor
public class ShelterController {
    private final ShelterService shelterService;
    private final ShelterMapper shelterMapper;

    @GetMapping
    public Page<ShelterResponse> getAll(@RequestParam(required = false) String city,
                                        @RequestParam(required = false) Boolean verified,
                                        @ParameterObject
                                        @PageableDefault(
                                                size = 20,
                                                sort = "createdAt",
                                                direction = Sort.Direction.DESC
                                        )
                                        Pageable pageable) {
        return shelterService.getAll(city, verified, pageable).map(shelterMapper::toResponse);
    }

    @GetMapping("/{id}")
    public ShelterResponse get(@PathVariable Long id) {
        Shelter shelter = shelterService.getById(id);
        return shelterMapper.toResponse(shelter);
    }

    @PostMapping
    public ResponseEntity<ShelterResponse> create(@Valid @RequestBody ShelterRequest request) {
        ModifyShelterCommand command = toCommand(null, request);

        Shelter shelter = shelterService.create(command);
        ShelterResponse shelterResponse = shelterMapper.toResponse(shelter);

        URI location = URI.create(String.format("/api/v1/shelters/%d", shelterResponse.id()));
        return ResponseEntity.created(location).body(shelterResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShelterResponse> update(@PathVariable Long id, @Valid @RequestBody ShelterRequest request) {
        ModifyShelterCommand command = toCommand(id, request);

        Shelter updatedShelter = shelterService.update(command);
        ShelterResponse shelterResponse = shelterMapper.toResponse(updatedShelter);
        return ResponseEntity.ok(shelterResponse);
    }

    @PatchMapping("/{id}/verify")
    public ResponseEntity<ShelterResponse> verify(@PathVariable Long id) {
        Shelter verifiedShelter = shelterService.verify(id);
        ShelterResponse shelterResponse = shelterMapper.toResponse(verifiedShelter);
        return ResponseEntity.ok(shelterResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        shelterService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private static ModifyShelterCommand toCommand(Long id, ShelterRequest request) {
        return new ModifyShelterCommand(
                id, request.name(), request.city(), request.address(),
                request.contactEmail(), request.contactPhone(), request.description()
        );
    }
}
