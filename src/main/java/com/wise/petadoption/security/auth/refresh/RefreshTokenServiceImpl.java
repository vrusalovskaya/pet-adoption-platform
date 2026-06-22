package com.wise.petadoption.security.auth.refresh;

import com.wise.petadoption.user.pesistence.UserEntity;
import com.wise.petadoption.user.pesistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private static final Integer VALIDITY_PERIOD_DAYS = 30;

    private final RefreshTokenRepository repository;
    private final UserRepository userRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    private final RefreshTokenEntityMapper mapper;
    private final HashCalculator hashCalculator;

    @Override
    @Transactional
    public String create(Long userId) {
        UserEntity userEntity = userRepository.getReferenceById(userId);
        String rawToken = generateToken();

        RefreshTokenEntity tokenEntity = new RefreshTokenEntity();
        tokenEntity.setToken(hashCalculator.calculate(rawToken));
        tokenEntity.setUserEntity(userEntity);
        tokenEntity.setExpiresAt(Instant.now().plus(VALIDITY_PERIOD_DAYS, ChronoUnit.DAYS));

        repository.save(tokenEntity);

        return rawToken;
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshToken findValidToken(String token) {
        String hash = hashCalculator.calculate(token);
        RefreshTokenEntity tokenEntity = repository.findByToken(hash)
                .orElseThrow(() -> new InvalidRefreshTokenException("Token not found"));

        if (tokenEntity.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidRefreshTokenException("Token is expired");
        }

        return mapper.toModel(tokenEntity);
    }

    @Override
    @Transactional
    public void deleteByToken(String token) {
        String hash = hashCalculator.calculate(token);
        if (repository.deleteByToken(hash) == 0) {
            throw new InvalidRefreshTokenException("Token not found");
        }
    }

    @Override
    @Transactional
    public void deleteByUserId(Long userId) {
        repository.deleteAllByUserEntityId(userId);
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
