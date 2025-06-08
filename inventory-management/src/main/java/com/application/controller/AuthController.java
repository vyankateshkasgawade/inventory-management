package com.application.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus; // Import HttpStatus
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.application.dto.ForgotPasswordRequest;
import com.application.dto.JwtAuthRequest;
import com.application.dto.JwtAuthResponse;
import com.application.dto.OtpVerificationRequest;
import com.application.dto.ResetPasswordRequest;
import com.application.entity.AppUser;
import com.application.repository.AppUserRepository;
import com.application.security.JwtTokenHelper;
import com.application.service.EmailSenderService;
import com.application.service.OtpStoreService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final JwtTokenHelper jwtTokenHelper;
	private final AppUserRepository appUserRepository;
	private final OtpStoreService otpStoreService;
	private final EmailSenderService emailSenderService;
	


    private final PasswordEncoder passwordEncoder;

 //=========================================== login ===========================================================================
    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponse> login(@RequestBody JwtAuthRequest request) {
        log.info("--- Entering login method ---");
        log.info("Attempting login for user: {}", request.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            log.info("Authentication successful for: {}", request.getUsername());

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // ✅ Fetch userId from the database
            AppUser appUser = appUserRepository.findByName(request.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // ✅ Pass userId into token generation
            String token = jwtTokenHelper.generateToken(userDetails, appUser.getUserId());

            log.info("Generated JWT token: {}", token);

            return ResponseEntity.ok(new JwtAuthResponse(token));

        } catch (AuthenticationException e) {
            log.error("Authentication failed for user: {}", request.getUsername(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } finally {
            log.info("--- Exiting login method ---");
        }
    }

//============================================= logout ============================================================================
    // Logout API
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        log.info("--- Entering logout method ---");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.error("Invalid Authorization header.");
            return ResponseEntity.badRequest().body("Invalid Authorization header.");
        }

        String token = authHeader.substring(7);
        log.info("Token extracted for logout: {}", token);

        jwtTokenHelper.blacklistToken(token);
        log.info("Token blacklisted successfully.");

        log.info("--- Exiting logout method ---");
        return ResponseEntity.ok("Logout successful.");
    }

   
//==================================== Forgot Password - Send OTP =========================================================
   
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        log.info("Attempting to send OTP for email: {}", request.getEmail());
        try {
            // Check if user exists BEFORE sending OTP for security and to prevent errors
            AppUser user = appUserRepository.findByEmail(request.getEmail()).orElse(null);

            if (user == null) {
                // For security reasons, don't confirm if an email exists or not.
                // Return a generic success message even if the user isn't found.
                // This prevents malicious actors from enumerating valid email addresses.
                log.warn("Forgot password request for non-existent email: {}", request.getEmail());
                return ResponseEntity.ok("If the email is registered, an OTP has been sent.");
            }

            String otp = String.valueOf(new Random().nextInt(900000) + 100000);
            otpStoreService.saveOtp(request.getEmail(), otp);
            emailSenderService.sendOtpEmail(request.getEmail(), otp);
            log.info("OTP sent to email: {}", request.getEmail());
            return ResponseEntity.ok("OTP sent to email.");

        } catch (Exception e) {
            log.error("Error processing forgot password request for email {}: {}", request.getEmail(), e.getMessage(), e);
            // Return a 500 status code for internal server errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send OTP. Please try again.");
        }
    }

 //============================================ verifyOtp =====================================================================================  

    // Verify OTP
    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody OtpVerificationRequest request) {
        log.info("Attempting to verify OTP for email: {}", request.getEmail());
        try {
            if (otpStoreService.verifyOtp(request.getEmail(), request.getOtp())) {
                log.info("OTP verified successfully for email: {}", request.getEmail());
                return ResponseEntity.ok("OTP verified successfully.");
            } else {
                log.warn("Invalid OTP attempt for email: {}", request.getEmail());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid OTP.");
            }
        } catch (Exception e) {
            log.error("Error during OTP verification for email {}: {}", request.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error verifying OTP. Please try again.");
        }
    }

 //=============================================== Reset Password =====================================================================================
    
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        log.info("Attempting to reset password for email: {}", request.getEmail());
        try {
            if (!otpStoreService.verifyOtp(request.getEmail(), request.getOtp())) {
                log.warn("Invalid OTP during password reset attempt for email: {}", request.getEmail());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid OTP or OTP expired.");
            }

            AppUser user = appUserRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> {
                        log.error("User not found during password reset for email: {}", request.getEmail());
                        return new UsernameNotFoundException("User not found with this email.");
                    });

            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            appUserRepository.save(user);
            otpStoreService.removeOtp(request.getEmail());
            log.info("Password reset successful for email: {}", request.getEmail());
            return ResponseEntity.ok("Password reset successful.");

        } catch (UsernameNotFoundException e) {
            // Catch UsernameNotFoundException specifically if findByEmail().orElseThrow() was used
            log.error("User not found during password reset for email {}: {}", request.getEmail(), e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
        catch (Exception e) {
            log.error("Error during password reset for email {}: {}", request.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to reset password. Please try again.");
        }
    }

//============================================= send-registration-otp =============================================================================
    @PostMapping("/send-registration-otp")
    public ResponseEntity<String> sendRegistrationOtp(@RequestBody ForgotPasswordRequest request) {
        log.info("Received OTP request for registration email: {}", request.getEmail());

        try {
            
            
            String otp = String.valueOf(new Random().nextInt(900000) + 100000);
            otpStoreService.saveOtp(request.getEmail(), otp);
            emailSenderService.sendOtpEmail(request.getEmail(), otp);
            return ResponseEntity.ok("OTP sent to email for registration.");
        } catch (Exception e) {
            log.error("Error sending registration OTP for email {}: {}", request.getEmail(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error sending OTP: " + e.getMessage());
        }
    }
}