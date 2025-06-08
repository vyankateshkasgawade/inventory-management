package com.application;


import org.junit.jupiter.api.Test;

import com.application.dto.JwtAuthResponse;

import static org.junit.jupiter.api.Assertions.*;

public class JwtAuthResponseTest {

    @Test
    public void testJwtAuthResponse() {
        // Create and test JwtAuthResponse
        JwtAuthResponse response = new JwtAuthResponse("testtoken");

        // Verify all fields
        assertEquals("testtoken", response.getToken());

        // Test toString
        assertNotNull(response.toString());
    }
}
