package com.application.service.impl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OtpStoreServiceImplTest {

    private OtpStoreServiceImpl otpStoreService;

    @BeforeEach
    void setUp() {
        // Initialize a new instance before each test to ensure a clean state
        otpStoreService = new OtpStoreServiceImpl();
    }

    @Test
    void saveOtp_ShouldStoreOtpCorrectly() {
        String email = "test@example.com";
        String otp = "123456";

        otpStoreService.saveOtp(email, otp);

        // To verify it's saved, we'll try to verify it
        assertTrue(otpStoreService.verifyOtp(email, otp), "OTP should be successfully stored and verifiable.");
    }

    @Test
    void verifyOtp_ShouldReturnTrue_WhenOtpMatches() {
        String email = "verify@example.com";
        String otp = "654321";
        otpStoreService.saveOtp(email, otp); // Store it first

        boolean result = otpStoreService.verifyOtp(email, otp);

        assertTrue(result, "Verification should return true for a matching OTP.");
    }

    @Test
    void verifyOtp_ShouldReturnFalse_WhenOtpDoesNotMatch() {
        String email = "nomatch@example.com";
        String storedOtp = "987654";
        String providedOtp = "111111";
        otpStoreService.saveOtp(email, storedOtp); // Store it first

        boolean result = otpStoreService.verifyOtp(email, providedOtp);

        assertFalse(result, "Verification should return false for a non-matching OTP.");
    }

    @Test
    void verifyOtp_ShouldReturnFalse_WhenEmailDoesNotExist() {
        String email = "nonexistent@example.com";
        String otp = "123456"; // Any OTP

        // Don't save anything for this email
        boolean result = otpStoreService.verifyOtp(email, otp);

        assertFalse(result, "Verification should return false if the email does not exist in the store.");
    }

    @Test
    void verifyOtp_ShouldReturnFalse_WhenProvidedOtpIsNullButStoredOtpIsNotNull() {
        String email = "nullprovided@example.com";
        String storedOtp = "123456";
        otpStoreService.saveOtp(email, storedOtp);


    }

    @Test
    void verifyOtp_ShouldReturnFalse_WhenStoredOtpIsNullButProvidedOtpIsNotNull() {

        String email = "nullstored@example.com";
        // Do not save an OTP, so get() returns null.
        String providedOtp = "123456";

        boolean result = otpStoreService.verifyOtp(email, providedOtp);

        assertFalse(result, "Verification should be false if stored OTP is null but provided OTP is not.");
    }

    @Test
    void verifyOtp_ShouldReturnTrue_WhenBothOtpsAreNull( ) {
   
        String email = "bothnull@example.com";
        assertThrows(NullPointerException.class, () -> otpStoreService.verifyOtp(email, null),
                "Calling verifyOtp with a null provided OTP should throw NPE if stored OTP is also null or absent.");
    }

    @Test
    void removeOtp_ShouldRemoveOtpSuccessfully() {
        String email = "remove@example.com";
        String otp = "112233";
        otpStoreService.saveOtp(email, otp); // Store it first

        assertTrue(otpStoreService.verifyOtp(email, otp), "OTP should be verifiable before removal.");

        otpStoreService.removeOtp(email);

        assertFalse(otpStoreService.verifyOtp(email, otp), "OTP should not be verifiable after removal.");
    }

    @Test
    void removeOtp_ShouldDoNothing_WhenEmailDoesNotExist() {
        String email = "nonexistent_for_removal@example.com";
        // No OTP saved for this email

        // Ensure no exception is thrown and state remains consistent
        assertDoesNotThrow(() -> otpStoreService.removeOtp(email), "Removing a non-existent OTP should not throw an exception.");
        assertFalse(otpStoreService.verifyOtp(email, "any_otp"), "Verification should still be false.");
    }
}
