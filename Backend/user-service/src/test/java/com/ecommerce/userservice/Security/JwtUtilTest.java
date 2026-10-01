package com.ecommerce.userservice.Security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for JwtUtil.
 * Covers: token generation, claim extraction, expiry validation, and invalid-token handling.
 * These tests drive coverage on the Security package to satisfy the SonarQube coverage gate.
 */
class JwtUtilTest {

    /** A 256-bit (32-char) secret required by HS256. */
    private static final String SECRET = "my-super-secret-key-for-testing!";
    private static final long ACCESS_EXPIRY_MS  = 3_600_000L;   // 1 hour
    private static final long REFRESH_EXPIRY_MS = 86_400_000L;  // 24 hours

    private JwtUtil jwtUtil;
    private UUID    userId;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET, ACCESS_EXPIRY_MS, REFRESH_EXPIRY_MS);
        userId  = UUID.randomUUID();
    }

    // ─── generateAccessToken ──────────────────────────────────────────────────

    @Test
    @DisplayName("generateAccessToken returns non-null, non-blank token")
    void generateAccessToken_returnsToken() {
        String token = jwtUtil.generateAccessToken(userId, "user@example.com", "USER");
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("generateAccessToken embeds correct email as subject")
    void generateAccessToken_subjectIsEmail() {
        String token = jwtUtil.generateAccessToken(userId, "user@example.com", "USER");
        assertThat(jwtUtil.extractEmail(token)).isEqualTo("user@example.com");
    }

    @Test
    @DisplayName("generateAccessToken embeds correct role claim")
    void generateAccessToken_roleClaimCorrect() {
        String token = jwtUtil.generateAccessToken(userId, "user@example.com", "ADMIN");
        assertThat(jwtUtil.extractRole(token)).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("generateAccessToken embeds correct userId claim")
    void generateAccessToken_userIdClaimCorrect() {
        String token = jwtUtil.generateAccessToken(userId, "user@example.com", "USER");
        assertThat(jwtUtil.extractUserId(token)).isEqualTo(userId.toString());
    }

    // ─── generateRefreshToken ─────────────────────────────────────────────────

    @Test
    @DisplayName("generateRefreshToken returns non-null, non-blank token")
    void generateRefreshToken_returnsToken() {
        String token = jwtUtil.generateRefreshToken(userId, "user@example.com", "USER");
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("generateRefreshToken embeds correct email as subject")
    void generateRefreshToken_subjectIsEmail() {
        String token = jwtUtil.generateRefreshToken(userId, "user@example.com", "USER");
        assertThat(jwtUtil.extractEmail(token)).isEqualTo("user@example.com");
    }

    // ─── isTokenValid ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("isTokenValid returns true for a freshly generated token")
    void isTokenValid_freshToken_returnsTrue() {
        String token = jwtUtil.generateAccessToken(userId, "user@example.com", "USER");
        assertThat(jwtUtil.isTokenValid(token)).isTrue();
    }

    @Test
    @DisplayName("isTokenValid returns false for a garbled/invalid token string")
    void isTokenValid_garbledToken_returnsFalse() {
        assertThat(jwtUtil.isTokenValid("this.is.not.a.real.jwt")).isFalse();
    }

    @Test
    @DisplayName("isTokenValid returns false for empty string")
    void isTokenValid_emptyString_returnsFalse() {
        assertThat(jwtUtil.isTokenValid("")).isFalse();
    }

    @Test
    @DisplayName("isTokenValid returns false for an expired token")
    void isTokenValid_expiredToken_returnsFalse() {
        // Create a JwtUtil with 0ms expiry so the token is immediately expired
        JwtUtil shortLived = new JwtUtil(SECRET, 0L, 0L);
        String expiredToken = shortLived.generateAccessToken(userId, "user@example.com", "USER");
        assertThat(jwtUtil.isTokenValid(expiredToken)).isFalse();
    }

    // ─── extractClaims ───────────────────────────────────────────────────────

    @Test
    @DisplayName("extractClaims returns non-null Claims object for a valid token")
    void extractClaims_validToken_returnsClaims() {
        String token  = jwtUtil.generateAccessToken(userId, "claims@test.com", "USER");
        Claims claims = jwtUtil.extractClaims(token);
        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo("claims@test.com");
    }

    @Test
    @DisplayName("extractClaims carries tokenType ACCESS for access tokens")
    void extractClaims_accessToken_hasCorrectTokenType() {
        String token  = jwtUtil.generateAccessToken(userId, "user@test.com", "USER");
        Claims claims = jwtUtil.extractClaims(token);
        assertThat(claims.get("tokenType", String.class)).isEqualTo("ACCESS");
    }

    @Test
    @DisplayName("extractClaims carries tokenType REFRESH for refresh tokens")
    void extractClaims_refreshToken_hasCorrectTokenType() {
        String token  = jwtUtil.generateRefreshToken(userId, "user@test.com", "USER");
        Claims claims = jwtUtil.extractClaims(token);
        assertThat(claims.get("tokenType", String.class)).isEqualTo("REFRESH");
    }
}
