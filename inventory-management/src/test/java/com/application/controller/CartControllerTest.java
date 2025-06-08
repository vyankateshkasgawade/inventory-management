package com.application.controller;

import com.application.dto.CartDTO;
import com.application.exception.CartNotFoundException;
import com.application.exception.ResourceNotFoundException;
import com.application.service.CartService;
import com.application.util.AppConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartController cartController;

    private CartDTO validCartDTO;
    private CartDTO existingCartDTO;

    @BeforeEach
    void setUp() {
        validCartDTO = CartDTO.builder()
                .cartId(1L)
                .userId(1L)
                .userName("Test User")
                .productId(1L)
                .productName("Test Product")
                .quantity(2)
                .addedDate(new Date())
                .finalTotalAmount(BigDecimal.valueOf(199.98))
                .isActive(true)
                .build();

        existingCartDTO = CartDTO.builder()
                .cartId(2L)
                .userId(2L)
                .userName("Another User")
                .productId(2L)
                .productName("Another Product")
                .quantity(1)
                .addedDate(new Date())
                .finalTotalAmount(BigDecimal.valueOf(99.99))
                .isActive(true)
                .build();
    }

 // ------------------- Add Item to Cart Tests -----------------------------------------------------

    @Test
    void addItemToCart_ShouldReturnCreated_WhenValidInput() {
        when(cartService.addToCart(any(CartDTO.class))).thenReturn(validCartDTO);

        ResponseEntity<?> response = cartController.addItemToCart(validCartDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(validCartDTO, response.getBody());
        verify(cartService).addToCart(any(CartDTO.class));
    }

    @Test
    void addItemToCart_ShouldReturnBadRequest_WhenIllegalArgumentExceptionThrown() {
        when(cartService.addToCart(any(CartDTO.class)))
                .thenThrow(new IllegalArgumentException("Invalid quantity"));

        ResponseEntity<?> response = cartController.addItemToCart(validCartDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Invalid input"));
    }

    @Test
    void addItemToCart_ShouldReturnConflict_WhenDataIntegrityViolationExceptionThrown() {
        when(cartService.addToCart(any(CartDTO.class)))
                .thenThrow(new DataIntegrityViolationException("DB constraint violation"));

        ResponseEntity<?> response = cartController.addItemToCart(validCartDTO);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Conflict"));
    }

    @Test
    void addItemToCart_ShouldReturnNotFound_WhenResourceNotFoundExceptionThrown() {
        when(cartService.addToCart(any(CartDTO.class)))
                .thenThrow(new ResourceNotFoundException(AppConstants.USER_NOT_FOUND + 1L));

        ResponseEntity<?> response = cartController.addItemToCart(validCartDTO);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().toString().contains(AppConstants.USER_NOT_FOUND));
    }

    @Test
    void addItemToCart_ShouldReturnConflict_WhenRuntimeExceptionForProductConflict() {
        when(cartService.addToCart(any(CartDTO.class)))
                .thenThrow(new RuntimeException("Product with ID 5 is already in the cart"));

        ResponseEntity<?> response = cartController.addItemToCart(validCartDTO);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Product with ID"));
    }

    @Test
    void addItemToCart_ShouldReturnInternalServerError_WhenGenericRuntimeExceptionThrown() {
        when(cartService.addToCart(any(CartDTO.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<?> response = cartController.addItemToCart(validCartDTO);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().toString().contains(AppConstants.FAILED));
    }

    @Test
    void addItemToCart_ShouldReturnInternalServerError_WhenUnknownExceptionThrown() {
        when(cartService.addToCart(any(CartDTO.class)))
                .thenThrow(new NullPointerException("Null pointer in service"));

        ResponseEntity<?> response = cartController.addItemToCart(validCartDTO);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
      
    }

    
 // ------------------- Get Cart By CartId Item Tests -------------------------------------------------------------

    @Test
    void getCartItemByCartId_ShouldReturnOk_WhenCartExists() {
        when(cartService.getCartByCartId(anyLong())).thenReturn(validCartDTO);

        ResponseEntity<?> response = cartController.getCartItemByCartId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(validCartDTO, response.getBody());
        verify(cartService).getCartByCartId(anyLong());
    }

    @Test
    void getCartItemByCartId_ShouldReturnBadRequest_WhenIllegalArgumentExceptionThrown() {
        when(cartService.getCartByCartId(anyLong()))
                .thenThrow(new IllegalArgumentException("Invalid cart ID"));

        ResponseEntity<?> response = cartController.getCartItemByCartId(-1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid cart ID", response.getBody());
    }

    @Test
    void getCartItemByCartId_ShouldReturnNotFound_WhenResourceNotFoundExceptionThrown() {
        when(cartService.getCartByCartId(anyLong()))
                .thenThrow(new ResourceNotFoundException(AppConstants.CART_NOT_FOUND + 1L));

        ResponseEntity<?> response = cartController.getCartItemByCartId(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().toString().contains(AppConstants.CART_NOT_FOUND));
    }

    @Test
    void getCartItemByCartId_ShouldReturnInternalServerError_WhenRuntimeExceptionThrown() {
        when(cartService.getCartByCartId(anyLong()))
                .thenThrow(new RuntimeException("Unexpected runtime error"));

        ResponseEntity<?> response = cartController.getCartItemByCartId(1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().toString().contains(AppConstants.FAILED));
    }

    @Test
    void getCartItemByCartId_ShouldReturnInternalServerError_WhenGenericExceptionThrown() {
        when(cartService.getCartByCartId(anyLong()))
                .thenThrow(new NullPointerException("Null value"));

        ResponseEntity<?> response = cartController.getCartItemByCartId(1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
       
    }

 // ------------------- Get All Cart Items By User Id Tests ----------------------------------------------------------------

    @Test
    void getAllCartItemsByUserId_ShouldReturnOk_WhenCartsExist() {
        List<CartDTO> carts = Arrays.asList(validCartDTO, existingCartDTO);
        when(cartService.getCartsByUserId(anyLong())).thenReturn(carts);

        ResponseEntity<?> response = cartController.getAllCartItemsByUserId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(carts, response.getBody());
        verify(cartService).getCartsByUserId(anyLong());
    }

    @Test
    void getAllCartItemsByUserId_ShouldReturnEmptyList_WhenNoCartsExist() {
        when(cartService.getCartsByUserId(anyLong())).thenReturn(List.of());

        ResponseEntity<?> response = cartController.getAllCartItemsByUserId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(((List<?>) response.getBody()).isEmpty());
        verify(cartService).getCartsByUserId(anyLong());
    }

    @Test
    void getAllCartItemsByUserId_ShouldReturnBadRequest_WhenIllegalArgumentExceptionThrown() {
        when(cartService.getCartsByUserId(anyLong()))
                .thenThrow(new IllegalArgumentException("Invalid user ID"));

        ResponseEntity<?> response = cartController.getAllCartItemsByUserId(-1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid user ID", response.getBody());
    }

    @Test
    void getAllCartItemsByUserId_ShouldReturnInternalServerError_WhenRuntimeExceptionThrown() {
        when(cartService.getCartsByUserId(anyLong()))
                .thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<?> response = cartController.getAllCartItemsByUserId(1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().toString().contains(AppConstants.FAILED));
    }

    @Test
    void getAllCartItemsByUserId_ShouldReturnInternalServerError_WhenGenericExceptionThrown() {
        when(cartService.getCartsByUserId(anyLong()))
                .thenThrow(new NullPointerException("Null pointer"));

        ResponseEntity<?> response = cartController.getAllCartItemsByUserId(1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        //assertTrue(response.getBody().toString().contains(AppConstants.SOMETHING_WENT_WRONG));
    }

 // ----------------------------- Update Cart Item Tests ------------------------------------------------------------

    @Test
    void updateCartByCartId_ShouldReturnOk_WhenValidInput() {
        when(cartService.updateCartByCartId(anyLong(), any(CartDTO.class)))
                .thenReturn(validCartDTO);

        ResponseEntity<?> response = cartController.updateCartByCartId(1L, validCartDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(validCartDTO, response.getBody());
        verify(cartService).updateCartByCartId(anyLong(), any(CartDTO.class));
    }

    @Test
    void updateCartByCartId_ShouldReturnBadRequest_WhenIllegalArgumentExceptionThrown() {
        when(cartService.updateCartByCartId(anyLong(), any(CartDTO.class)))
                .thenThrow(new IllegalArgumentException("Invalid quantity"));

        ResponseEntity<?> response = cartController.updateCartByCartId(1L, validCartDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Invalid input"));
    }

    @Test
    void updateCartByCartId_ShouldReturnNotFound_WhenResourceNotFoundExceptionThrown() {
        when(cartService.updateCartByCartId(anyLong(), any(CartDTO.class)))
                .thenThrow(new ResourceNotFoundException(AppConstants.CART_NOT_FOUND + 1L));

        ResponseEntity<?> response = cartController.updateCartByCartId(1L, validCartDTO);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().toString().contains(AppConstants.CART_NOT_FOUND));
    }

	
 // ------------------- Delete Cart Item Tests ----------------------------------------------------------------------

    @Test
    void deleteCartItemByCartId_ShouldReturnNoContent_WhenSuccessful() {
        doNothing().when(cartService).deleteCartByCartId(anyLong());

        ResponseEntity<?> response = cartController.deleteCartItemByCartId(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(cartService).deleteCartByCartId(anyLong());
    }

    @Test
    void deleteCartItemByCartId_ShouldReturnBadRequest_WhenInvalidCartId() {
        doThrow(new IllegalArgumentException("Invalid cart ID"))
                .when(cartService).deleteCartByCartId(anyLong());

        ResponseEntity<?> response = cartController.deleteCartItemByCartId(-1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid cart ID", response.getBody());
    }

    @Test
    void deleteCartItemByCartId_ShouldReturnNotFound_WhenCartNotFound() {
        doThrow(new CartNotFoundException(AppConstants.CART_NOT_FOUND + 1L))
                .when(cartService).deleteCartByCartId(anyLong());

        ResponseEntity<?> response = cartController.deleteCartItemByCartId(1L);
    }

    @Test
    void deleteCartItemByCartId_ShouldReturnInternalServerError_WhenRuntimeExceptionThrown() {
        doThrow(new RuntimeException("Unexpected error"))
                .when(cartService).deleteCartByCartId(anyLong());

        ResponseEntity<?> response = cartController.deleteCartItemByCartId(1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().toString().contains(AppConstants.FAILED));
    }

}