package com.wise.petadoption.security.persistence;

import com.wise.petadoption.support.AbstractPostgresIT;
import com.wise.petadoption.user.common.Role;
import com.wise.petadoption.user.persistence.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RefreshTokenRepositoryIT extends AbstractPostgresIT {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private TestEntityManager entityManager;

    private UserEntity persistUser(String email) {
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPasswordHash("hashed");
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setRole(Role.ROLE_USER);
        return entityManager.persistAndFlush(user);
    }

    private RefreshTokenEntity persistToken(UserEntity user, String hash) {
        return persistToken(user, hash, Instant.now().plus(30, ChronoUnit.DAYS));
    }

    private RefreshTokenEntity persistToken(UserEntity user, String hash, Instant expiresAt) {
        RefreshTokenEntity token = new RefreshTokenEntity();
        token.setUserEntity(user);
        token.setTokenHash(hash);
        token.setExpiresAt(expiresAt);
        return entityManager.persistAndFlush(token);
    }

    @Test
    void findByTokenHash_ExistingToken_ReturnsToken() {
        UserEntity user = persistUser("token-owner@example.com");
        persistToken(user, "hash-1");

        assertThat(refreshTokenRepository.findByTokenHash("hash-1"))
                .get()
                .extracting(token -> token.getUserEntity().getId())
                .isEqualTo(user.getId());
    }

    @Test
    void deleteByTokenHash_ExistingToken_RemovesToken() {
        UserEntity user = persistUser("delete-token@example.com");
        persistToken(user, "hash-to-delete");

        refreshTokenRepository.deleteByTokenHash("hash-to-delete");
        entityManager.flush();
        entityManager.clear();

        assertThat(refreshTokenRepository.findByTokenHash("hash-to-delete")).isEmpty();
    }

    @Test
    void deleteAllByUserEntityId_UserWithTokens_RemovesAllUserTokens() {
        UserEntity user = persistUser("multi-token@example.com");
        persistToken(user, "hash-a");
        persistToken(user, "hash-b");

        refreshTokenRepository.deleteAllByUserEntityId(user.getId());
        entityManager.flush();
        entityManager.clear();

        assertThat(refreshTokenRepository.findByTokenHash("hash-a")).isEmpty();
        assertThat(refreshTokenRepository.findByTokenHash("hash-b")).isEmpty();
    }

    @Test
    void deleteAllByExpiresAtBefore_ExpiredTokens_RemovesOnlyExpiredTokens() {
        UserEntity user = persistUser("expired-cleanup@example.com");
        persistToken(user, "expired-hash", Instant.now().minus(1, ChronoUnit.DAYS));
        persistToken(user, "valid-hash", Instant.now().plus(1, ChronoUnit.DAYS));

        refreshTokenRepository.deleteAllByExpiresAtBefore(Instant.now());
        entityManager.flush();
        entityManager.clear();

        assertThat(refreshTokenRepository.findByTokenHash("expired-hash")).isEmpty();
        assertThat(refreshTokenRepository.findByTokenHash("valid-hash")).isPresent();
    }
}
