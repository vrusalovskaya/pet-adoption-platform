package com.wise.petadoption.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupScheduler {
    private final RefreshTokenCleanupService cleanupService;

    @Scheduled(cron = "${security.refresh-token.cleanup-cron}")
    public void deleteExpiredTokens() {
        cleanupService.deleteExpiredTokens();
    }
}
