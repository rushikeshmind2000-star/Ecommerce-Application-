package com.ecommerce.userservice.Security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

/**
 * Utility for generating and validating JWT tokens.
 * The secret must be at least 32 chars (256-bit for HS256).
 */
@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long accessTokenExpiryMs;
    private final long refreshTokenExpiryMs;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiry-ms:3600000}") long accessTokenExpiryMs,
            @Value("${jwt.refresh-token-expiry-ms:86400000}") long refreshTokenExpiryMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpiryMs = accessTokenExpiryMs;
        this.refreshTokenExpiryMs = refreshTokenExpiryMs;
    }

    /** Generate access token (short-lived) */
    public String generateAccessToken(UUID userId, String email, String role) {
        return buildToken(userId, email, role, accessTokenExpiryMs, "ACCESS");
    }

    /** Generate refresh token (long-lived) */
    public String generateRefreshToken(UUID userId, String email, String role) {
        return buildToken(userId, email, role, refreshTokenExpiryMs, "REFRESH");
    }

    private String buildToken(UUID userId, String email, String role, long expiryMs, String tokenType) {
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId.toString())
                .claim("role", role)
                .claim("tokenType", tokenType)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiryMs))
                .signWith(secretKey)
                .compact();
    }

    /** Extract all claims from a token */
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    public String extractUserId(String token) {
        return extractClaims(token).get("userId", String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
