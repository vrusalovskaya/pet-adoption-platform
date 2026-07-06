package com.wise.petadoption.adoption.controller;

import com.wise.petadoption.adoption.api.ApplicationRequest;
import com.wise.petadoption.adoption.api.ApplicationResponse;
import com.wise.petadoption.adoption.api.RejectionRequest;
import com.wise.petadoption.adoption.common.ApplicationStatus;
import com.wise.petadoption.adoption.domain.Application;
import com.wise.petadoption.adoption.domain.CreateApplicationCommand;
import com.wise.petadoption.adoption.domain.RejectionCommand;
import com.wise.petadoption.adoption.mapper.ApplicationResponseMapper;
import com.wise.petadoption.adoption.service.ApplicationService;
import com.wise.petadoption.security.domain.SecurityUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
public class ApplicationController {
    private final ApplicationService applicationService;
    private final ApplicationResponseMapper responseMapper;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<ApplicationResponse> getAll(@RequestParam(required = false) ApplicationStatus status,
                                            @RequestParam(required = false) Long animalId,
                                            @RequestParam(required = false) Long applicantId,
                                            @ParameterObject
                                            @PageableDefault(
                                                    size = 20,
                                                    sort = "createdAt",
                                                    direction = Sort.Direction.DESC
                                            )
                                            Pageable pageable
    ) {
        return applicationService.getAll(status, animalId, applicantId, pageable).map(responseMapper::toResponse);
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApplicationResponse getForAdmin(@PathVariable Long id) {
        return responseMapper.toResponse(applicationService.getForAdmin(id));
    }

    @PatchMapping("/admin/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApplicationResponse> approve(@PathVariable Long id) {
        Application application = applicationService.approve(id);
        return ResponseEntity.ok(responseMapper.toResponse(application));
    }

    @PatchMapping("/admin/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApplicationResponse> reject(@PathVariable Long id,
                                                      @Valid @RequestBody RejectionRequest request) {
        Application application = applicationService.reject(toCommand(id, request));
        return ResponseEntity.ok(responseMapper.toResponse(application));
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApplicationResponse> create(@Valid @RequestBody ApplicationRequest request,
                                                      @AuthenticationPrincipal SecurityUser user) {
        CreateApplicationCommand command = toCommand(request, user.getUserId());

        Application application = applicationService.create(command);
        ApplicationResponse applicationResponse = responseMapper.toResponse(application);

        URI location = URI.create(String.format("/api/v1/applications/%d", applicationResponse.id()));
        return ResponseEntity.created(location).body(applicationResponse);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public Page<ApplicationResponse> getMy(@ParameterObject
                                           @PageableDefault(
                                                   size = 20,
                                                   sort = "createdAt",
                                                   direction = Sort.Direction.DESC
                                           )
                                           Pageable pageable,
                                           @AuthenticationPrincipal SecurityUser user
    ) {
        return applicationService.getAllByApplicant(user.getUserId(), pageable).map(responseMapper::toResponse);
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ApplicationResponse getForUser(@PathVariable Long id,
                                          @AuthenticationPrincipal SecurityUser user) {
        return responseMapper.toResponse(applicationService.getForUser(id, user.getUserId()));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApplicationResponse> cancel(@PathVariable Long id,
                                                      @AuthenticationPrincipal SecurityUser user) {
        Application application = applicationService.cancel(id, user.getUserId());
        return ResponseEntity.ok(responseMapper.toResponse(application));
    }

    CreateApplicationCommand toCommand(ApplicationRequest request, Long applicantId) {
        return new CreateApplicationCommand(request.animalId(), applicantId, request.message());
    }

    RejectionCommand toCommand(Long id, RejectionRequest request) {
        return new RejectionCommand(id, request.decisionComment());
    }
}
