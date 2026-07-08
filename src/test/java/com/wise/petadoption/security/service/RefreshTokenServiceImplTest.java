package com.wise.petadoption.security.service;

import com.wise.petadoption.security.config.RefreshTokenProperties;
import com.wise.petadoption.security.domain.RefreshTokenRotationResult;
import com.wise.petadoption.security.exception.InvalidRefreshTokenException;
import com.wise.petadoption.security.persistence.RefreshTokenEntity;
import com.wise.petadoption.security.persistence.RefreshTokenRepository;
import com.wise.petadoption.user.common.Role;
import com.wise.petadoption.user.persistence.UserEntity;
import com.wise.petadoption.user.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static com.wise.petadoption.support.TestFixtures.userEntity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository repository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private HashCalculator hashCalculator;

    private RefreshTokenServiceImpl refreshTokenService;

    @BeforeEach
    void setUp() {
        RefreshTokenProperties properties = new RefreshTokenProperties();
        properties.setTtl(Duration.ofDays(30));

        refreshTokenService = new RefreshTokenServiceImpl(
                properties,
                repository,
                userRepository,
                hashCalculator
        );
    }

    @Test
    void create_ValidUser_PersistsHashedTokenAndReturnsRawToken() {
        UserEntity userEntity = userEntity(1L, "jane@example.com", Role.ROLE_USER);
        when(userRepository.getReferenceById(1L)).thenReturn(userEntity);
        when(hashCalculator.calculate(anyString())).thenReturn("token-hash");

        String rawToken = refreshTokenService.create(1L);

        assertThat(rawToken).isNotBlank();
        assertThat(rawToken).isNotEqualTo("token-hash");
        ArgumentCaptor<RefreshTokenEntity> captor = ArgumentCaptor.forClass(RefreshTokenEntity.class);
        verify(repository).save(captor.capture());
        RefreshTokenEntity saved = captor.getValue();
        assertThat(saved.getTokenHash()).isEqualTo("token-hash");
        assertThat(saved.getUserEntity()).isEqualTo(userEntity);
        assertThat(saved.getExpiresAt()).isAfter(Instant.now());
    }

    @Test
    void rotate_ValidToken_DeletesOldTokenAndReturnsNewToken() {
        UserEntity userEntity = userEntity(1L, "jane@example.com", Role.ROLE_USER);
        RefreshTokenEntity existing = new RefreshTokenEntity();
        existing.setTokenHash("old-hash");
        existing.setUserEntity(userEntity);
        existing.setExpiresAt(Instant.now().plusSeconds(3600));
        when(hashCalculator.calculate("old-token")).thenReturn("old-hash");
        when(hashCalculator.calculate(argThat(value -> !"old-token".equals(value)))).thenReturn("new-hash");
        when(repository.findByTokenHash("old-hash")).thenReturn(Optional.of(existing));

        RefreshTokenRotationResult result = refreshTokenService.rotate("old-token");

        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.rawToken()).isNotBlank();
        verify(repository).deleteByTokenHash("old-hash");
        verify(repository).save(argThat(entity -> "new-hash".equals(entity.getTokenHash())));
    }

    @Test
    void rotate_UnknownToken_ThrowsInvalidRefreshTokenException() {
        when(hashCalculator.calculate("missing-token")).thenReturn("missing-hash");
        when(repository.findByTokenHash("missing-hash")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.rotate("missing-token"))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("not found");
        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void rotate_ExpiredToken_DeletesTokenAndThrows() {
        RefreshTokenEntity expired = new RefreshTokenEntity();
        expired.setTokenHash("expired-hash");
        expired.setUserEntity(userEntity(1L, "jane@example.com", Role.ROLE_USER));
        expired.setExpiresAt(Instant.now().minusSeconds(3600));
        when(hashCalculator.calculate("expired-token")).thenReturn("expired-hash");
        when(repository.findByTokenHash("expired-hash")).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> refreshTokenService.rotate("expired-token"))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("expired");
        verify(repository, times(1)).deleteByTokenHash("expired-hash");
        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void deleteByTokenIfExists_AnyToken_HashesTokenThenDeletesByHash() {
        when(hashCalculator.calculate("some-token")).thenReturn("some-hash");

        refreshTokenService.deleteByTokenIfExists("some-token");

        verify(repository).deleteByTokenHash("some-hash");
    }
}
