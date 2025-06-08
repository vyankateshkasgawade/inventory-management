package com.application.security;

import static org.mockito.Mockito.*;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;

class JwtAuthenticationEntryPointTest {

    private JwtAuthenticationEntryPoint entryPoint;

    @BeforeEach
    void setUp() {
        entryPoint = new JwtAuthenticationEntryPoint();
    }

    @Test
    void commence_ShouldSendUnauthorizedError() throws IOException, ServletException {
        // Arrange
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        AuthenticationException mockException = mock(AuthenticationException.class);

        // Act
        entryPoint.commence(mockRequest, mockResponse, mockException);

        // Assert
        verify(mockResponse).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }
}
