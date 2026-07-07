package com.wise.petadoption.security.service;

import com.wise.petadoption.security.config.RefreshTokenProperties;
import com.wise.petadoption.security.domain.RefreshTokenRotationResult;
import com.wise.petadoption.security.exception.InvalidRefreshTokenException;
import com.wise.petadoption.security.persistence.RefreshTokenEntity;
import com.wise.petadoption.security.persistence.RefreshTokenRepository;
import com.wise.petadoption.user.persistence.UserEntity;
import com.wise.petadoption.user.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private final RefreshTokenProperties refreshTokenProperties;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final HashCalculator hashCalculator;

    @Override
    @Transactional
    public String create(Long userId) {
        String rawToken = generateToken();
        UserEntity userEntity = userRepository.getReferenceById(userId);
        RefreshTokenEntity tokenEntity = buildTokenEntity(userEntity, rawToken);

        refreshTokenRepository.save(tokenEntity);

        return rawToken;
    }

    @Override
    @Transactional
    public RefreshTokenRotationResult rotate(String token) {
        String hash = hashCalculator.calculate(token);
        RefreshTokenEntity tokenEntity = loadAndValidateToken(hash);

        refreshTokenRepository.deleteByTokenHash(hash);

        String rawToken = generateToken();
        UserEntity userEntity = tokenEntity.getUserEntity();
        RefreshTokenEntity newEntity = buildTokenEntity(userEntity, rawToken);

        refreshTokenRepository.save(newEntity);

        return new RefreshTokenRotationResult(rawToken, userEntity.getId());
    }

    @Override
    @Transactional
    public void deleteByTokenIfExists(String token) {
        String hash = hashCalculator.calculate(token);
        refreshTokenRepository.deleteByTokenHash(hash);
    }

    @Override
    @Transactional
    public void deleteByUserId(Long userId) {
        refreshTokenRepository.deleteAllByUserEntityId(userId);
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private RefreshTokenEntity buildTokenEntity(UserEntity userEntity, String rawToken) {
        RefreshTokenEntity tokenEntity = new RefreshTokenEntity();
        tokenEntity.setTokenHash(hashCalculator.calculate(rawToken));
        tokenEntity.setUserEntity(userEntity);
        tokenEntity.setExpiresAt(Instant.now().plus(refreshTokenProperties.getTtl()));

        return tokenEntity;
    }

    private RefreshTokenEntity loadAndValidateToken(String hash) {
        RefreshTokenEntity tokenEntity = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new InvalidRefreshTokenException("Token not found"));

        if (tokenEntity.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.deleteByTokenHash(hash);
            throw new InvalidRefreshTokenException("Token is expired");
        }

        return tokenEntity;
    }
}
