package com.wise.petadoption.security.service;

import com.wise.petadoption.security.persistence.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RefreshTokenCleanupServiceTest {

    @Mock
    private RefreshTokenRepository repository;

    @Test
    void deleteExpiredTokens_RemovesTokensExpiredBeforeCurrentTime() {
        RefreshTokenCleanupService service = new RefreshTokenCleanupService(repository);
        Instant beforeCleanup = Instant.now();

        service.deleteExpiredTokens();

        ArgumentCaptor<Instant> captor = ArgumentCaptor.forClass(Instant.class);
        verify(repository).deleteAllByExpiresAtBefore(captor.capture());
        assertThat(captor.getValue()).isAfterOrEqualTo(beforeCleanup);
        assertThat(captor.getValue()).isBeforeOrEqualTo(Instant.now());
    }
}
