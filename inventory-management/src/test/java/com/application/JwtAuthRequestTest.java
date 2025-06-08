package com.application;

import org.junit.jupiter.api.Test;

import com.application.dto.JwtAuthRequest;

import static org.junit.jupiter.api.Assertions.*;

public class JwtAuthRequestTest {

    @Test
    public void testJwtAuthRequest() {
        // Create and test JwtAuthRequest
        JwtAuthRequest request = new JwtAuthRequest();
        request.setUsername("testuser");
        request.setPassword("testpass");

        // Verify all fields
        assertEquals("testuser", request.getUsername());
        assertEquals("testpass", request.getPassword());

        // Test toString
        assertNotNull(request.toString());
    }
}