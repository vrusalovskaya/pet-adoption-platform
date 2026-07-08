package com.wise.petadoption.security.service;

import com.wise.petadoption.shared.scheduler.cleanup.RefreshTokenCleanupScheduler;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RefreshTokenCleanupSchedulerTest {

    @Test
    void deleteExpiredTokens_DelegatesCleanupToService() {
        RefreshTokenCleanupService cleanupService = mock(RefreshTokenCleanupService.class);
        RefreshTokenCleanupScheduler scheduler = new RefreshTokenCleanupScheduler(cleanupService);

        scheduler.deleteExpiredTokens();

        verify(cleanupService).deleteExpiredTokens();
    }
}
