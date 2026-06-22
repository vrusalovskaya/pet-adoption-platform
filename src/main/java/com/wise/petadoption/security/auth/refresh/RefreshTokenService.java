package com.wise.petadoption.security.auth.refresh;

public interface RefreshTokenService {
    String create(Long userId);

    RefreshToken findValidToken(String token);

    void deleteByToken(String token);

    void deleteByUserId(Long userId);
}
