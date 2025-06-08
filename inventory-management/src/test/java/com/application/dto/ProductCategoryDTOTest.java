package com.application.dto;



import org.junit.jupiter.api.Test;

import com.application.dto.ProductCategoryDTO;

import static org.junit.jupiter.api.Assertions.*;

public class ProductCategoryDTOTest {

    @Test
    public void testProductCategoryDTO() {
        // Create and test ProductCategoryDTO
        ProductCategoryDTO categoryDTO = ProductCategoryDTO.builder()
                .productCategoryId(1L)
                .productCategoryName("Electronics")
                .isActive(true)
                .build();

        // Verify all fields
        assertEquals(1L, categoryDTO.getProductCategoryId());
        assertEquals("Electronics", categoryDTO.getProductCategoryName());
        assertTrue(categoryDTO.getIsActive());

        // Test toString
        assertNotNull(categoryDTO.toString());
    }
}