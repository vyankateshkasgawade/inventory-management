package com.application.dto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OtpVerificationRequestTest {

    private OtpVerificationRequest request;

    @BeforeEach
    void setUp() {
        // Initialize a common OtpVerificationRequest object for each test
        request = OtpVerificationRequest.builder()
                .email("test@example.com")
                .otp("123456")
                .build();
    }

    @Test
    void testNoArgsConstructor() {
        OtpVerificationRequest newRequest = new OtpVerificationRequest();
        assertNotNull(newRequest);
        assertNull(newRequest.getEmail());
        assertNull(newRequest.getOtp());
    }

    @Test
    void testAllArgsConstructor() {
        OtpVerificationRequest allArgsRequest = new OtpVerificationRequest("another@example.com", "654321");
        assertNotNull(allArgsRequest);
        assertEquals("another@example.com", allArgsRequest.getEmail());
        assertEquals("654321", allArgsRequest.getOtp());
    }

    @Test
    void testBuilder() {
        assertNotNull(request);
        assertEquals("test@example.com", request.getEmail());
        assertEquals("123456", request.getOtp());
    }

    @Test
    void testGettersAndSetters() {
        OtpVerificationRequest testRequest = new OtpVerificationRequest();

        testRequest.setEmail("updated@example.com");
        assertEquals("updated@example.com", testRequest.getEmail());

        testRequest.setOtp("987654");
        assertEquals("987654", testRequest.getOtp());
    }

    @Test
    void testEqualsAndHashCode() {
        // Create an identical DTO
        OtpVerificationRequest sameRequest = OtpVerificationRequest.builder()
                .email("test@example.com")
                .otp("123456")
                .build();

        // Create a DTO with a different email
        OtpVerificationRequest differentEmailRequest = OtpVerificationRequest.builder()
                .email("different@example.com")
                .otp("123456")
                .build();

        // Create a DTO with a different OTP
        OtpVerificationRequest differentOtpRequest = OtpVerificationRequest.builder()
                .email("test@example.com")
                .otp("999999")
                .build();

        // Test equality
        assertEquals(request, sameRequest);
        assertEquals(request.hashCode(), sameRequest.hashCode());

        // Test inequality
        assertNotEquals(request, differentEmailRequest);
        assertNotEquals(request.hashCode(), differentEmailRequest.hashCode());

        assertNotEquals(request, differentOtpRequest);
        assertNotEquals(request.hashCode(), differentOtpRequest.hashCode());

        // Test with nulls for one or both fields
        OtpVerificationRequest nullEmail = OtpVerificationRequest.builder().email(null).otp("123456").build();
        assertNotEquals(request, nullEmail);

        OtpVerificationRequest nullOtp = OtpVerificationRequest.builder().email("test@example.com").otp(null).build();
        assertNotEquals(request, nullOtp);

        OtpVerificationRequest allNull = OtpVerificationRequest.builder().email(null).otp(null).build();
        assertNotEquals(request, allNull); // Original is not all null
        assertEquals(OtpVerificationRequest.builder().email(null).otp(null).build(), allNull); // all nulls should equal each other

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
    }
}
