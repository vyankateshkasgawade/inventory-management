package com.application.dto;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JwtAuthRequestTest {

    private JwtAuthRequest jwtAuthRequest;

    @BeforeEach
    void setUp() {
        // Initialize a common JwtAuthRequest object for each test
        jwtAuthRequest = new JwtAuthRequest("testuser", "testpassword");
    }

    @Test
    void testNoArgsConstructor() {
        JwtAuthRequest newRequest = new JwtAuthRequest();
        assertNotNull(newRequest);
        assertNull(newRequest.getUsername());
        assertNull(newRequest.getPassword());
    }

    @Test
    void testAllArgsConstructor() {
        JwtAuthRequest allArgsRequest = new JwtAuthRequest("anotheruser", "anotherpass");
        assertNotNull(allArgsRequest);
        assertEquals("anotheruser", allArgsRequest.getUsername());
        assertEquals("anotherpass", allArgsRequest.getPassword());
    }



    @Test
    void testGettersAndSetters() {
        JwtAuthRequest testRequest = new JwtAuthRequest();

        testRequest.setUsername("setteruser");
        assertEquals("setteruser", testRequest.getUsername());

        testRequest.setPassword("setterpass");
        assertEquals("setterpass", testRequest.getPassword());
    }

    @Test
    void testEqualsAndHashCode() {
        // Create an identical DTO
        JwtAuthRequest sameRequest = new JwtAuthRequest("testuser", "testpassword");

        // Create a DTO with a different username
        JwtAuthRequest differentUsernameRequest = new JwtAuthRequest("diffuser", "testpassword");

        // Create a DTO with a different password
        JwtAuthRequest differentPasswordRequest = new JwtAuthRequest("testuser", "diffpass");

        // Test equality
        assertEquals(jwtAuthRequest, sameRequest);
        assertEquals(jwtAuthRequest.hashCode(), sameRequest.hashCode());

        // Test inequality
        assertNotEquals(jwtAuthRequest, differentUsernameRequest);
        assertNotEquals(jwtAuthRequest.hashCode(), differentUsernameRequest.hashCode());

        assertNotEquals(jwtAuthRequest, differentPasswordRequest);
        assertNotEquals(jwtAuthRequest.hashCode(), differentPasswordRequest.hashCode());

        // Test with nulls for one or both fields
        JwtAuthRequest nullUsername = new JwtAuthRequest(null, "testpassword");
        assertNotEquals(jwtAuthRequest, nullUsername);

        JwtAuthRequest nullPassword = new JwtAuthRequest("testuser", null);
        assertNotEquals(jwtAuthRequest, nullPassword);

        JwtAuthRequest allNull = new JwtAuthRequest(null, null);
        assertNotEquals(jwtAuthRequest, allNull); // Original is not all null
        assertEquals(new JwtAuthRequest(null, null), allNull); // all nulls should equal each other

        // Test with nulls and different object types
        assertFalse(jwtAuthRequest.equals(null));
        assertFalse(jwtAuthRequest.equals(new Object()));
    }

    @Test
    void testToString() {
        // Just assert that toString() doesn't throw an error and contains the relevant data
        String dtoString = jwtAuthRequest.toString();
        assertNotNull(dtoString);
        assertTrue(dtoString.contains("username=testuser"));
        assertTrue(dtoString.contains("password=testpassword"));
    }
}
