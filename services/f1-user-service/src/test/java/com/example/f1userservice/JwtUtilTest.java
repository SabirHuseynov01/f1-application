package com.example.f1userservice;

import com.example.f1userservice.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtUtil Tests")
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private static final String SECRET = "test-secret-key-that-is-long-enough-for-hs512-algorithm-2026";
    private static final long EXPIRATION = 86400000;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET, EXPIRATION);
    }

    @Test
    @DisplayName("generateToken - creates valid JWT token")
    void generateToken_createsValidToken() {
        String token = jwtUtil.generateToken("verstappen", "USER");

        assertThat(token).isNotNull();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("validateToken - valid token returns true")
    void validateToken_validToken_returnsTrue() {
        String token = jwtUtil.generateToken("hamilton", "ADMIN");

        assertThat(jwtUtil.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("validateToken - tampered token returns false")
    void validateToken_tamperedToken_returnsFalse() {
        String token = jwtUtil.generateToken("user", "USER") + "tampered";

        assertThat(jwtUtil.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("validateToken - completely invalid token returns false")
    void validateToken_invalidToken_returnsFalse() {
        assertThat(jwtUtil.validateToken("not.a.token")).isFalse();
    }

    @Test
    @DisplayName("extractUsername - returns correct username")
    void extractUsername_returnsCorrectUsername() {
        String token = jwtUtil.generateToken("norris", "USER");

        assertThat(jwtUtil.extractUsername(token)).isEqualTo("norris");
    }

    @Test
    @DisplayName("extractRole - returns correct role")
    void extractRole_returnsCorrectRole() {
        String token = jwtUtil.generateToken("piastri", "MODERATOR");

        assertThat(jwtUtil.extractRole(token)).isEqualTo("MODERATOR");
    }

    @Test
    @DisplayName("getExpirationDate - returns future date")
    void getExpirationDate_returnsFutureDate() {
        String token = jwtUtil.generateToken("russell", "USER");
        Date expiration = jwtUtil.getExpirationDate(token);

        assertThat(expiration).isAfter(new Date());
    }

    @Test
    @DisplayName("validateToken - expired token throws ExpiredJwtException")
    void validateToken_expiredToken_throws() {
        JwtUtil shortLivedJwt = new JwtUtil(SECRET, -1000);
        String token = shortLivedJwt.generateToken("user", "USER");

        assertThat(jwtUtil.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("generateToken - different users generate different tokens")
    void generateToken_differentUsers_differentTokens() {
        String token1 = jwtUtil.generateToken("user1", "USER");
        String token2 = jwtUtil.generateToken("user2", "USER");

        assertThat(token1).isNotEqualTo(token2);
    }
}
