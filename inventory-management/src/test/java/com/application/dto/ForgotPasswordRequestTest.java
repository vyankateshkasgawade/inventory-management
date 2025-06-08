package com.application.dto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ForgotPasswordRequestTest {

    private ForgotPasswordRequest request;

    @BeforeEach
    void setUp() {
        // Initialize a common ForgotPasswordRequest object for each test
        request = ForgotPasswordRequest.builder()
                .email("test@example.com")
                .build();
    }

    @Test
    void testNoArgsConstructor() {
        ForgotPasswordRequest newRequest = new ForgotPasswordRequest();
        assertNotNull(newRequest);
        assertNull(newRequest.getEmail());
    }

    @Test
    void testAllArgsConstructor() {
        ForgotPasswordRequest allArgsRequest = new ForgotPasswordRequest("another@example.com");
        assertNotNull(allArgsRequest);
        assertEquals("another@example.com", allArgsRequest.getEmail());
    }

    @Test
    void testBuilder() {
        assertNotNull(request);
        assertEquals("test@example.com", request.getEmail());
    }

    @Test
    void testGettersAndSetters() {
        ForgotPasswordRequest testRequest = new ForgotPasswordRequest();
        testRequest.setEmail("updated@example.com");
        assertEquals("updated@example.com", testRequest.getEmail());
    }

    @Test
    void testEqualsAndHashCode() {
        // Create an identical DTO
        ForgotPasswordRequest sameRequest = ForgotPasswordRequest.builder()
                .email("test@example.com")
                .build();

        // Create a DTO with a different value
        ForgotPasswordRequest differentRequest = ForgotPasswordRequest.builder()
                .email("different@example.com")
                .build();

        // Test equality
        assertEquals(request, sameRequest);
        assertEquals(request.hashCode(), sameRequest.hashCode());

        // Test inequality
        assertNotEquals(request, differentRequest);
        assertNotEquals(request.hashCode(), differentRequest.hashCode());

        // Test with nulls and different object types
        assertFalse(request.equals(null));
        assertFalse(request.equals(new Object()));
    }

    @Test
    void testToString() {
        // Just assert that toString() doesn't throw an error and contains the relevant data
        String dtoString = request.toString();
        assertNotNull(dtoString);
        assertTrue(dtoString.contains("email=test@example.com"));
    }
}