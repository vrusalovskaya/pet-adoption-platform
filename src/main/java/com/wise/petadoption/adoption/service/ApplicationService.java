package com.wise.petadoption.adoption.service;

import com.wise.petadoption.adoption.common.ApplicationStatus;
import com.wise.petadoption.adoption.domain.Application;
import com.wise.petadoption.adoption.domain.CreateApplicationCommand;
import com.wise.petadoption.adoption.domain.RejectionCommand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ApplicationService {
    Application create(CreateApplicationCommand command);

    Page<Application> getAllByApplicant(Long applicantId, Pageable pageable);

    Page<Application> getAll(ApplicationStatus status, Long animalId, Long applicantId,
                             Pageable pageable);

    Application getForAdmin(Long id);

    Application getForUser(Long id, Long userId);

    Application approve(Long id);

    Application reject(RejectionCommand command);

    Application cancel(Long id, Long userId);
}
