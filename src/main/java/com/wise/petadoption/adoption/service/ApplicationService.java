package com.wise.petadoption.adoption.service;

import com.wise.petadoption.adoption.common.ApplicationStatus;
import com.wise.petadoption.adoption.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ApplicationService {
    Application create(CreateApplicationCommand command);

    Page<Application> getAllByApplicant(Long applicantId, Pageable pageable);

    Page<Application> getAll(ApplicationStatus status, Long animalId, Long applicantId,
                             Pageable pageable);

    Application get(Long id, CurrentUser currentUser);

    Application approve(Long id);

    Application reject(RejectionCommand command);

    Application cancel(Long id, CurrentUser currentUser);
}
