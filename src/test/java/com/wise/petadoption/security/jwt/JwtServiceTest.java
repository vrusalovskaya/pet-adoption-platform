package com.wise.petadoption.security.jwt;

import com.wise.petadoption.security.domain.SecurityUser;
import com.wise.petadoption.user.common.Role;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static com.wise.petadoption.support.TestFixtures.securityUser;
import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("very-strong-secret-key-should-be-at-least-32-bytes");
        properties.setAccessTokenTtl(Duration.ofMinutes(15));

        jwtService = new JwtService(properties);
    }

    @Test
    void generateAccessToken_ValidUser_EmbedsSubjectAndCustomClaims() {
        SecurityUser user = securityUser(42L, "jane@example.com", Role.ROLE_USER);

        String token = jwtService.generateAccessToken(user);
        Claims claims = jwtService.extractClaims(token);

        assertThat(claims.getSubject()).isEqualTo("jane@example.com");
        assertThat(claims.get("userId", Long.class)).isEqualTo(42L);
        assertThat(claims.get("name", String.class)).isEqualTo("Jane Doe");
    }

    @Test
    void extractUsername_GeneratedToken_ReturnsEmail() {
        SecurityUser user = securityUser(1L, "jane@example.com", Role.ROLE_USER);
        String token = jwtService.generateAccessToken(user);

        String username = jwtService.extractUsername(token);

        assertThat(username).isEqualTo("jane@example.com");
    }

    @Test
    void isValid_GeneratedToken_ReturnsTrue() {
        SecurityUser user = securityUser(1L, "jane@example.com", Role.ROLE_USER);
        String token = jwtService.generateAccessToken(user);

        assertThat(jwtService.isValid(token)).isTrue();
    }

    @Test
    void isValid_MalformedToken_ReturnsFalse() {
        assertThat(jwtService.isValid("this-is-not-a-jwt")).isFalse();
    }

    @Test
    void isValid_TamperedToken_ReturnsFalse() {
        SecurityUser user = securityUser(1L, "jane@example.com", Role.ROLE_USER);
        String token = jwtService.generateAccessToken(user);
        String tampered = token.substring(0, token.length() - 2) + "xy";

        assertThat(jwtService.isValid(tampered)).isFalse();
    }
}
