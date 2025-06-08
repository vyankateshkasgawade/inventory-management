package com.application.dto;


import org.junit.jupiter.api.Test;

import com.application.dto.CartDTO;

import java.math.BigDecimal;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

public class CartDTOTest {

    @Test
    public void testCartDTO() {
        // Create test data
        Date addedDate = new Date();

        // Create and test CartDTO
        CartDTO cartDTO = CartDTO.builder()
                .cartId(1L)
                .userId(1L)
                .userName("Test User")
                .productId(1L)
                .productName("Test Product")
                .quantity(2)
                .addedDate(addedDate)
                .finalTotalAmount(new BigDecimal("99.99"))
                .isActive(true)
                .build();

        // Verify all fields
        assertEquals(1L, cartDTO.getCartId());
        assertEquals(1L, cartDTO.getUserId());
        assertEquals("Test User", cartDTO.getUserName());
        assertEquals(1L, cartDTO.getProductId());
        assertEquals("Test Product", cartDTO.getProductName());
        assertEquals(2, cartDTO.getQuantity());
        assertEquals(addedDate, cartDTO.getAddedDate());
        assertEquals(new BigDecimal("99.99"), cartDTO.getFinalTotalAmount());
        assertTrue(cartDTO.getIsActive());

        // Test toString
        assertNotNull(cartDTO.toString());
    }
}