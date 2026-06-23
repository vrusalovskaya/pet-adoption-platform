package com.wise.petadoption.security.service;

import com.wise.petadoption.security.domain.RefreshTokenRotationResult;

public interface RefreshTokenService {
    String create(Long userId);

    RefreshTokenRotationResult rotate(String token);

    void deleteByTokenIfExists(String token);

    void deleteByUserId(Long userId);
}
