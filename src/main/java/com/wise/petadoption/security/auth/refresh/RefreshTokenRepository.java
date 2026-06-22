package com.wise.petadoption.security.auth.refresh;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    Optional<RefreshTokenEntity> findByToken(String token);

    @Modifying
    @Query("""
                delete from RefreshTokenEntity rt
                where rt.token = :token
            """)
    int deleteByToken(String token);

    void deleteAllByUserEntityId(Long userId);
}
