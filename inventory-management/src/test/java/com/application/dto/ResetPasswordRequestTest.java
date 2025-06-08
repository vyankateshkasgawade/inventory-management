package com.application.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ResetPasswordRequestTest {

    private ResetPasswordRequest request;

    @BeforeEach
    void setUp() {
        // Initialize a common ResetPasswordRequest object for each test
        request = ResetPasswordRequest.builder()
                .email("test@example.com")
                .otp("123456")
                .newPassword("newStrongPassword123")
                .build();
    }

    @Test
    void testNoArgsConstructor() {
        ResetPasswordRequest newRequest = new ResetPasswordRequest();
        assertNotNull(newRequest);
        assertNull(newRequest.getEmail());
        assertNull(newRequest.getOtp());
        assertNull(newRequest.getNewPassword());
    }

    @Test
    void testAllArgsConstructor() {
        ResetPasswordRequest allArgsRequest = new ResetPasswordRequest(
                "another@example.com", "654321", "anotherStrongPass"
        );
        assertNotNull(allArgsRequest);
        assertEquals("another@example.com", allArgsRequest.getEmail());
        assertEquals("654321", allArgsRequest.getOtp());
        assertEquals("anotherStrongPass", allArgsRequest.getNewPassword());
    }

    @Test
    void testBuilder() {
        assertNotNull(request);
        assertEquals("test@example.com", request.getEmail());
        assertEquals("123456", request.getOtp());
        assertEquals("newStrongPassword123", request.getNewPassword());
    }

    @Test
    void testGettersAndSetters() {
        ResetPasswordRequest testRequest = new ResetPasswordRequest();

        testRequest.setEmail("updated@example.com");
        assertEquals("updated@example.com", testRequest.getEmail());

        testRequest.setOtp("987654");
        assertEquals("987654", testRequest.getOtp());

        testRequest.setNewPassword("reallyNewPassword");
        assertEquals("reallyNewPassword", testRequest.getNewPassword());
    }

    @Test
    void testEqualsAndHashCode() {
        // Create an identical DTO
        ResetPasswordRequest sameRequest = ResetPasswordRequest.builder()
                .email("test@example.com")
                .otp("123456")
                .newPassword("newStrongPassword123")
                .build();

        // Create a DTO with a different email
        ResetPasswordRequest differentEmailRequest = ResetPasswordRequest.builder()
                .email("different@example.com")
                .otp("123456")
                .newPassword("newStrongPassword123")
                .build();

        // Create a DTO with a different OTP
        ResetPasswordRequest differentOtpRequest = ResetPasswordRequest.builder()
                .email("test@example.com")
                .otp("999999")
                .newPassword("newStrongPassword123")
                .build();

        // Create a DTO with a different new password
        ResetPasswordRequest differentPasswordRequest = ResetPasswordRequest.builder()
                .email("test@example.com")
                .otp("123456")
                .newPassword("anotherPass")
                .build();

        // Test equality
        assertEquals(request, sameRequest);
        assertEquals(request.hashCode(), sameRequest.hashCode());

        // Test inequality
        assertNotEquals(request, differentEmailRequest);
        assertNotEquals(request.hashCode(), differentEmailRequest.hashCode());

        assertNotEquals(request, differentOtpRequest);
        assertNotEquals(request.hashCode(), differentOtpRequest.hashCode());

        assertNotEquals(request, differentPasswordRequest);
        assertNotEquals(request.hashCode(), differentPasswordRequest.hashCode());

        // Test with nulls for one or more fields
        ResetPasswordRequest nullEmail = ResetPasswordRequest.builder().email(null).otp("123456").newPassword("pass").build();
        assertNotEquals(request, nullEmail);

        ResetPasswordRequest nullOtp = ResetPasswordRequest.builder().email("e@e.com").otp(null).newPassword("pass").build();
        assertNotEquals(request, nullOtp);

        ResetPasswordRequest nullPassword = ResetPasswordRequest.builder().email("e@e.com").otp("123").newPassword(null).build();
        assertNotEquals(request, nullPassword);

        ResetPasswordRequest allNull = ResetPasswordRequest.builder().email(null).otp(null).newPassword(null).build();
        assertEquals(ResetPasswordRequest.builder().email(null).otp(null).newPassword(null).build(), allNull);

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
        assertTrue(dtoString.contains("otp=123456"));
        assertTrue(dtoString.contains("newPassword=newStrongPassword123"));
    }
}