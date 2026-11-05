package com.polink.common.security;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "7f9a8b2c4d6e1f3a5b7c9d0e2f4a6b8c0d1e3f5a7b9c1d3e5f7a9b0c2d4e6f8a";

    @Test
    void generateTokenAndExtractPrincipalShouldPreserveClaims() {
        JwtService jwtService = new JwtService(SECRET, 3600000L);
        UUID userId = UUID.randomUUID();
        Set<String> roles = Set.of("ROLE_STUDENT", "ROLE_TUTOR");

        String token = jwtService.generateToken(userId, "tutor@polink.edu.co", roles);

        assertThat(jwtService.isTokenValid(token)).isTrue();
        SecurityUserPrincipal principal = jwtService.extractPrincipal(token);
        assertThat(principal.getUserId()).isEqualTo(userId);
        assertThat(principal.getEmail()).isEqualTo("tutor@polink.edu.co");
        assertThat(principal.getRoleNames()).containsExactlyInAnyOrder("ROLE_STUDENT", "ROLE_TUTOR");
    }

    @Test
    void isTokenValidShouldReturnFalseForTamperedOrExpiredTokens() {
        JwtService activeService = new JwtService(SECRET, 3600000L);
        JwtService expiredService = new JwtService(SECRET, -1000L);

        String validToken = activeService.generateToken(UUID.randomUUID(), "a@polink.edu.co", Set.of("ROLE_STUDENT"));
        String tamperedToken = validToken + "tampered";
        String expiredToken = expiredService.generateToken(UUID.randomUUID(), "a@polink.edu.co", Set.of("ROLE_STUDENT"));

        assertThat(activeService.isTokenValid(tamperedToken)).isFalse();
        assertThat(activeService.isTokenValid(expiredToken)).isFalse();
        assertThat(activeService.isTokenValid("not-a-jwt")).isFalse();
    }
}
