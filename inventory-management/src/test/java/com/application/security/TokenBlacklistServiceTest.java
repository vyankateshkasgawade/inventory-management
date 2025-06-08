package com.application.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenBlacklistServiceTest {

    private TokenBlacklistService tokenBlacklistService;

    @BeforeEach
    void setUp() {
        tokenBlacklistService = new TokenBlacklistService();
    }

    @Test
    void blacklistToken_ShouldAddTokenToBlacklist() {
        String token = "test-token-123";

        tokenBlacklistService.blacklistToken(token);

        assertTrue(tokenBlacklistService.isBlacklisted(token));
    }

    @Test
    void isBlacklisted_ShouldReturnFalse_ForNonBlacklistedToken() {
        String token = "non-blacklisted-token";

        assertFalse(tokenBlacklistService.isBlacklisted(token));
    }
}

