package com.searchplatform.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        String secret = "test-secret-key-that-is-long-enough-for-hmac-sha256";
        jwtService = new JwtService(secret, 3600000L);
    }

    @Test
    void generateToken_shouldReturnNonNullToken() {
        String token = jwtService.generateToken("testuser");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void validateToken_shouldReturnTrueForValidToken() {
        String token = jwtService.generateToken("testuser");
        assertTrue(jwtService.validateToken(token));
    }

    @Test
    void validateToken_shouldReturnFalseForInvalidToken() {
        assertFalse(jwtService.validateToken("invalid.token.value"));
    }

    @Test
    void validateToken_shouldReturnFalseForNullToken() {
        assertFalse(jwtService.validateToken(null));
    }

    @Test
    void validateToken_shouldReturnFalseForEmptyToken() {
        assertFalse(jwtService.validateToken(""));
    }

    @Test
    void extractUsername_shouldReturnCorrectUsername() {
        String token = jwtService.generateToken("testuser");
        String username = jwtService.extractUsername(token);
        assertEquals("testuser", username);
    }

    @Test
    void validateToken_shouldReturnFalseForExpiredToken() {
        JwtService shortLivedService = new JwtService(
                "test-secret-key-that-is-long-enough-for-hmac-sha256", 0L);
        String token = shortLivedService.generateToken("testuser");
        assertFalse(shortLivedService.validateToken(token));
    }

    @Test
    void generateToken_differentUsersShouldGetDifferentTokens() {
        String token1 = jwtService.generateToken("user1");
        String token2 = jwtService.generateToken("user2");
        assertFalse(token1.equals(token2));
    }
}
