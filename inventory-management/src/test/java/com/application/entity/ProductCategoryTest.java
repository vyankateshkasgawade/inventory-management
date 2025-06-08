package com.application.entity;


import org.junit.jupiter.api.Test;

import com.application.entity.ProductCategory;
import com.application.entity.ProductDetails;

import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class ProductCategoryTest {

    @Test
    public void testProductCategoryEntity() {
        // Create test data
        List<ProductDetails> products = new ArrayList<>();

        // Create and test ProductCategory
        ProductCategory category = ProductCategory.builder()
                .productCategoryId(1L)
                .productCategoryName("Electronics")
                .isActive(true)
                .productDetails(products)
                .build();

        // Verify all fields
        assertEquals(1L, category.getProductCategoryId());
        assertEquals("Electronics", category.getProductCategoryName());
        assertTrue(category.getIsActive());
        assertEquals(products, category.getProductDetails());

        // Test toString
        assertNotNull(category.toString());
    }
}