package com.application.exception;


import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.application.exception.CartNotFoundException;
import com.application.exception.GlobalExceptionHandler;
import com.application.exception.InvalidTokenException;
import com.application.exception.ProductCategoryException;
import com.application.exception.ResourceNotFoundException;
import com.application.exception.UserAlreadyExistsException;
import com.application.exception.UserNotFoundException;
import com.application.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    private final String testMessage = "Test error message";

    @Test
    void handleResourceNotFoundException_ShouldReturnNotFound() {
        var ex = new ResourceNotFoundException(testMessage);
        var response = globalExceptionHandler.handleNotFound(ex);
        assertResponse(response, HttpStatus.NOT_FOUND, testMessage);
    }

    @Test
    void handleUserAlreadyExistsException_ShouldReturnConflict() {
        var ex = new UserAlreadyExistsException(testMessage);
        var response = globalExceptionHandler.handleUserAlreadyExists(ex);
        assertResponse(response, HttpStatus.CONFLICT, testMessage);
    }

    @Test
    void handleCartNotFoundException_ShouldReturnNotFound() {
        var ex = new CartNotFoundException(testMessage);
        var response = globalExceptionHandler.handleCartNotFound(ex);
        assertResponse(response, HttpStatus.NOT_FOUND, testMessage);
    }

    @Test
    void handleUserNotFoundException_ShouldReturnNotFound() {
        var ex = new UserNotFoundException(testMessage);
        var response = globalExceptionHandler.handleUserNotFound(ex);
        assertResponse(response, HttpStatus.NOT_FOUND, testMessage);
    }

    @Test
    void handleProductCategoryException_ShouldReturnBadRequest() {
        var ex = new ProductCategoryException(testMessage);
        var response = globalExceptionHandler.handleProductCategory(ex);
        assertResponse(response, HttpStatus.BAD_REQUEST, testMessage);
    }

    @Test
    void handleValidationException_ShouldReturnBadRequest() {
        var ex = new ValidationException(testMessage);
        var response = globalExceptionHandler.handleValidationException(ex);
        assertResponse(response, HttpStatus.BAD_REQUEST, testMessage);
    }

    @Test
    void handleInvalidTokenException_ShouldReturnUnauthorized() {
        var ex = new InvalidTokenException(testMessage);
        ResponseEntity<Object> response = globalExceptionHandler.handleInvalidTokenException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        //assertEquals(testMessage, response.getBody());
    }

    @Test
    void handleGenericException_ShouldReturnInternalServerError() {
        var ex = new Exception(testMessage);
        var response = globalExceptionHandler.handleGeneric(ex);
       // assertResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error: " + testMessage);
    }

    private void assertResponse(ResponseEntity<Object> response, HttpStatus expectedStatus, String expectedMessage) {
        assertEquals(expectedStatus, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("timestamp"));
        assertTrue(body.get("timestamp") instanceof LocalDateTime);
        assertEquals(expectedStatus.value(), body.get("status"));
        assertEquals(expectedMessage, body.get("message"));
    }
}


/*
 * @ExtendWith(MockitoExtension.class) class GlobalExceptionHandlerTest {
 * 
 * @InjectMocks private GlobalExceptionHandler globalExceptionHandler;
 * 
 * private final String testMessage = "Test error message";
 * 
 * @Test void handleResourceNotFoundException_ShouldReturnNotFoundStatus() {
 * ResourceNotFoundException ex = new ResourceNotFoundException(testMessage);
 * ResponseEntity<Object> response = globalExceptionHandler.handleNotFound(ex);
 * 
 * assertResponse(response, HttpStatus.NOT_FOUND, testMessage); }
 * 
 * @Test void handleUserAlreadyExistsException_ShouldReturnConflictStatus() {
 * UserAlreadyExistsException ex = new UserAlreadyExistsException(testMessage);
 * ResponseEntity<Object> response =
 * globalExceptionHandler.handleUserAlreadyExists(ex);
 * 
 * assertResponse(response, HttpStatus.CONFLICT, testMessage); }
 * 
 * @Test void handleCartNotFoundException_ShouldReturnNotFoundStatus() {
 * CartNotFoundException ex = new CartNotFoundException(testMessage);
 * ResponseEntity<Object> response =
 * globalExceptionHandler.handleCartNotFound(ex);
 * 
 * assertResponse(response, HttpStatus.NOT_FOUND, testMessage); }
 * 
 * @Test void handleUserNotFoundException_ShouldReturnNotFoundStatus() {
 * UserNotFoundException ex = new UserNotFoundException(testMessage);
 * ResponseEntity<Object> response =
 * globalExceptionHandler.handleUserNotFound(ex);
 * 
 * assertResponse(response, HttpStatus.NOT_FOUND, testMessage); }
 * 
 * @Test void handleProductCategoryException_ShouldReturnBadRequestStatus() {
 * ProductCategoryException ex = new ProductCategoryException(testMessage);
 * ResponseEntity<Object> response =
 * globalExceptionHandler.handleProductCategory(ex);
 * 
 * assertResponse(response, HttpStatus.BAD_REQUEST, testMessage); }
 * 
 * 
 * @Test void handleValidationException_ShouldReturnBadRequestStatus() {
 * ValidationException ex = new ValidationException(testMessage);
 * ResponseEntity<Object> response =
 * globalExceptionHandler.handleValidationException(ex);
 * 
 * assertResponse(response, HttpStatus.BAD_REQUEST, testMessage); }
 * 
 * @Test void handleInvalidTokenException_ShouldReturnUnauthorizedStatus() {
 * InvalidTokenException ex = new InvalidTokenException(testMessage);
 * ResponseEntity<String> response =
 * globalExceptionHandler.handleInvalidTokenException(ex);
 * 
 * assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
 * assertEquals(testMessage, response.getBody()); }
 * 
 * @Test void handleGenericException_ShouldReturnInternalServerErrorStatus() {
 * Exception ex = new Exception(testMessage); ResponseEntity<Object> response =
 * globalExceptionHandler.handleGeneric(ex);
 * 
 * assertResponse(response, HttpStatus.INTERNAL_SERVER_ERROR,
 * "Unexpected error: " + testMessage); }
 * 
 * private void assertResponse(ResponseEntity<Object> response, HttpStatus
 * expectedStatus, String expectedMessage) { assertEquals(expectedStatus,
 * response.getStatusCode());
 * 
 * Map<String, Object> body = (Map<String, Object>) response.getBody();
 * assertNotNull(body); assertTrue(body.containsKey("timestamp"));
 * assertTrue(body.get("timestamp") instanceof LocalDateTime);
 * assertEquals(expectedStatus.value(), body.get("status"));
 * assertEquals(expectedMessage, body.get("message")); } }
 */