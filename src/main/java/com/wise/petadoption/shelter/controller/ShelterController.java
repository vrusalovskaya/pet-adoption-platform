package com.wise.petadoption.shelter.controller;

import com.wise.petadoption.shelter.api.ShelterRequest;
import com.wise.petadoption.shelter.api.ShelterResponse;
import com.wise.petadoption.shelter.domain.CreateShelterCommand;
import com.wise.petadoption.shelter.domain.UpdateShelterCommand;
import com.wise.petadoption.shelter.domain.Shelter;
import com.wise.petadoption.shelter.mapper.ShelterResponseMapper;
import com.wise.petadoption.shelter.service.ShelterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/shelters")
@RequiredArgsConstructor
public class ShelterController {
    private final ShelterService shelterService;
    private final ShelterResponseMapper responseMapper;

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
        return shelterService.getAll(city, verified, pageable).map(responseMapper::toResponse);
    }

    @GetMapping("/{id}")
    public ShelterResponse get(@PathVariable Long id) {
        Shelter shelter = shelterService.get(id);
        return responseMapper.toResponse(shelter);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ShelterResponse> create(@Valid @RequestBody ShelterRequest request) {
        CreateShelterCommand command = toCommand(request);

        Shelter shelter = shelterService.create(command);
        ShelterResponse shelterResponse = responseMapper.toResponse(shelter);

        URI location = URI.create(String.format("/api/v1/shelters/%d", shelterResponse.id()));
        return ResponseEntity.created(location).body(shelterResponse);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ShelterResponse> update(@PathVariable Long id, @Valid @RequestBody ShelterRequest request) {
        UpdateShelterCommand command = toCommand(id, request);

        Shelter updatedShelter = shelterService.update(command);
        ShelterResponse shelterResponse = responseMapper.toResponse(updatedShelter);
        return ResponseEntity.ok(shelterResponse);
    }

    @PatchMapping("/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ShelterResponse> verify(@PathVariable Long id) {
        Shelter verifiedShelter = shelterService.verify(id);
        ShelterResponse shelterResponse = responseMapper.toResponse(verifiedShelter);
        return ResponseEntity.ok(shelterResponse);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        shelterService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private static CreateShelterCommand toCommand(ShelterRequest request) {
        return new CreateShelterCommand(
                request.name(), request.city(), request.address(),
                request.contactEmail(), request.contactPhone(), request.description()
        );
    }

    private static UpdateShelterCommand toCommand(Long id, ShelterRequest request) {
        return new UpdateShelterCommand(
                id, request.name(), request.city(), request.address(),
                request.contactEmail(), request.contactPhone(), request.description()
        );
    }
}
