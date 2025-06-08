package com.application.dto;


import org.junit.jupiter.api.Test;

import com.application.dto.PurchaseDetailsDTO;

import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

public class PurchaseDetailsDTOTest {

    @Test
    public void testPurchaseDetailsDTO() {
        // Create test data
        Date purchaseDate = new Date();

        // Create and test PurchaseDetailsDTO using builder
        PurchaseDetailsDTO dto = PurchaseDetailsDTO.builder()
                .purchaseDetailsId(1L)
                .userId(10L)
                .userName("John Doe")
                .productId(5L)
                .productName("Smartphone")
                .productCount(2)
                .purchaseDate(purchaseDate)
                .isActive(true)
                .build();

        // Verify all fields
        assertEquals(1L, dto.getPurchaseDetailsId());
        assertEquals(10L, dto.getUserId());
        assertEquals("John Doe", dto.getUserName());
        assertEquals(5L, dto.getProductId());
        assertEquals("Smartphone", dto.getProductName());
        assertEquals(2, dto.getProductCount());
        assertEquals(purchaseDate, dto.getPurchaseDate());
        assertTrue(dto.getIsActive());

        // Test no-args constructor and setters
        PurchaseDetailsDTO dto2 = new PurchaseDetailsDTO();
        dto2.setPurchaseDetailsId(2L);
        dto2.setUserId(11L);
        dto2.setUserName("Jane Smith");
        dto2.setProductId(6L);
        dto2.setProductName("Laptop");
        dto2.setProductCount(1);
        dto2.setPurchaseDate(purchaseDate);
        dto2.setIsActive(false);

        assertEquals(2L, dto2.getPurchaseDetailsId());
        assertEquals(11L, dto2.getUserId());
        assertEquals("Jane Smith", dto2.getUserName());
        assertEquals(6L, dto2.getProductId());
        assertEquals("Laptop", dto2.getProductName());
        assertEquals(1, dto2.getProductCount());
        assertEquals(purchaseDate, dto2.getPurchaseDate());
        assertFalse(dto2.getIsActive());

        // Test toString
        assertNotNull(dto.toString());
        assertNotNull(dto2.toString());

        // Test equals and hashCode
        PurchaseDetailsDTO dto3 = PurchaseDetailsDTO.builder()
                .purchaseDetailsId(1L)
                .userId(10L)
                .userName("John Doe")
                .productId(5L)
                .productName("Smartphone")
                .productCount(2)
                .purchaseDate(purchaseDate)
                .isActive(true)
                .build();

        assertEquals(dto, dto3);
        assertEquals(dto.hashCode(), dto3.hashCode());
    }

    @Test
    public void testEdgeCases() {
        // Test null values
        PurchaseDetailsDTO dto = PurchaseDetailsDTO.builder()
                .purchaseDetailsId(null)
                .userId(null)
                .userName(null)
                .productId(null)
                .productName(null)
                .productCount(null)
                .purchaseDate(null)
                .isActive(null)
                .build();

        assertNull(dto.getPurchaseDetailsId());
        assertNull(dto.getUserId());
        assertNull(dto.getUserName());
        assertNull(dto.getProductId());
        assertNull(dto.getProductName());
        assertNull(dto.getProductCount());
        assertNull(dto.getPurchaseDate());
        assertNull(dto.getIsActive());
    }
}