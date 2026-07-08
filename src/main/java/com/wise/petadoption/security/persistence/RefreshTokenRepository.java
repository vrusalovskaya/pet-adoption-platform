package com.wise.petadoption.security.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    Optional<RefreshTokenEntity> findByTokenHash(String token);

    void deleteByTokenHash(String token);

    void deleteAllByUserEntityId(Long userId);

    void deleteAllByExpiresAtBefore(Instant now);
}
