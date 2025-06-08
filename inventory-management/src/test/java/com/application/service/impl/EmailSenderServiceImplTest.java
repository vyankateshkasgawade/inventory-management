package com.application.service.impl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailSenderServiceImplTest {

    @InjectMocks
    private EmailSenderServiceImpl emailSenderService;

    @Mock
    private JavaMailSender mailSender;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize mocks
    }

    @Test
    void sendOtpEmail_ShouldConstructAndSendCorrectEmail() {
        // Given
        String toEmail = "test@example.com";
        String otp = "123456";

        // When
        emailSenderService.sendOtpEmail(toEmail, otp);

        // Then
        // 1. Verify that mailSender.send() was called exactly once
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));

        // 2. Capture the SimpleMailMessage that was sent to verify its content
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();

        // 3. Assert the content of the sent message
        assertNotNull(sentMessage);
        assertEquals(toEmail, sentMessage.getTo()[0], "Recipient email should match");
        assertEquals("Your OTP for Password Reset", sentMessage.getSubject(), "Subject should match");
        assertTrue(sentMessage.getText().contains("Use this OTP to reset your password: " + otp),
                "Email text should contain the correct OTP message");
    }

    @Test
    void sendOtpEmail_ShouldHandleNullToEmail() {
        
        String toEmail = null;
        String otp = "112233";
        assertDoesNotThrow(() -> emailSenderService.sendOtpEmail(toEmail, otp));

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        
    }

    @Test
    void sendOtpEmail_ShouldHandleNullOtp() {
        String toEmail = "user@example.com";
        String otp = null;

        assertDoesNotThrow(() -> emailSenderService.sendOtpEmail(toEmail, otp));

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertTrue(sentMessage.getText().contains("Use this OTP to reset your password: null")); // "null" string will be in text
    }

    
}