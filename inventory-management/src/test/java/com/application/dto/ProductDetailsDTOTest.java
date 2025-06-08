package com.application.dto;


import org.junit.jupiter.api.Test;

import com.application.dto.ProductDetailsDTO;

import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

public class ProductDetailsDTOTest {

    @Test
    public void testProductDetailsDTO() {
        // Create and test ProductDetailsDTO using builder
        ProductDetailsDTO dto = ProductDetailsDTO.builder()
                .productDetailsId(1L)
                .productName("Smartphone")
                .productQuantity(100)
                .price(new BigDecimal("599.99"))
                .productCategoryId(2L)
                .productCategoryName("Electronics")
                .isActive(true)
                .build();

        // Verify all fields
        assertEquals(1L, dto.getProductDetailsId());
        assertEquals("Smartphone", dto.getProductName());
        assertEquals(100, dto.getProductQuantity());
        assertEquals(new BigDecimal("599.99"), dto.getPrice());
        assertEquals(2L, dto.getProductCategoryId());
        assertEquals("Electronics", dto.getProductCategoryName());
        assertTrue(dto.getIsActive());

        // Test no-args constructor and setters
        ProductDetailsDTO dto2 = new ProductDetailsDTO();
        dto2.setProductDetailsId(2L);
        dto2.setProductName("Laptop");
        dto2.setProductQuantity(50);
        dto2.setPrice(new BigDecimal("1299.99"));
        dto2.setProductCategoryId(2L);
        dto2.setProductCategoryName("Electronics");
        dto2.setIsActive(false);

        assertEquals(2L, dto2.getProductDetailsId());
        assertEquals("Laptop", dto2.getProductName());
        assertEquals(50, dto2.getProductQuantity());
        assertEquals(new BigDecimal("1299.99"), dto2.getPrice());
        assertEquals(2L, dto2.getProductCategoryId());
        assertEquals("Electronics", dto2.getProductCategoryName());
        assertFalse(dto2.getIsActive());

        // Test toString
        assertNotNull(dto.toString());
        assertNotNull(dto2.toString());

        // Test equals and hashCode
        ProductDetailsDTO dto3 = ProductDetailsDTO.builder()
                .productDetailsId(1L)
                .productName("Smartphone")
                .productQuantity(100)
                .price(new BigDecimal("599.99"))
                .productCategoryId(2L)
                .productCategoryName("Electronics")
                .isActive(true)
                .build();

        assertEquals(dto, dto3);
        assertEquals(dto.hashCode(), dto3.hashCode());
    }

    @Test
    public void testEdgeCases() {
        // Test null values
        ProductDetailsDTO dto = ProductDetailsDTO.builder()
                .productDetailsId(null)
                .productName(null)
                .productQuantity(null)
                .price(null)
                .productCategoryId(null)
                .productCategoryName(null)
                .isActive(null)
                .build();

        assertNull(dto.getProductDetailsId());
        assertNull(dto.getProductName());
        assertNull(dto.getProductQuantity());
        assertNull(dto.getPrice());
        assertNull(dto.getProductCategoryId());
        assertNull(dto.getProductCategoryName());
        assertNull(dto.getIsActive());
    }
}