package com.application.entity;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

public class ProductDetailsTest {

    @Test
    public void testProductDetailsEntity() {
        // Create test data for ProductCategory
        ProductCategory category = new ProductCategory();
        category.setProductCategoryId(101L); // Set an ID for the category for completeness
        category.setProductCategoryName("Electronics");

        // Create and test ProductDetails using the builder pattern
        ProductDetails product = ProductDetails.builder()
                .productDetailsId(1L)
                .productName("Smartphone X")
                .productQuantity(100)
                .price(new BigDecimal("599.99"))
                .isActive(true)
                .productCategory(category)
                .imageUrl("http://example.com/images/smartphone_x.jpg")
                .trendingDisplayOrder(1) // Set a value for trendingDisplayOrder
                .sellingDisplayOrder(5)  // Set a value for sellingDisplayOrder
                .build();

        // Verify all fields
        assertEquals(1L, product.getProductDetailsId());
        assertEquals("Smartphone X", product.getProductName());
        assertEquals(100, product.getProductQuantity());
        assertEquals(new BigDecimal("599.99"), product.getPrice());
        assertTrue(product.getIsActive());
        assertEquals(category, product.getProductCategory());
        assertEquals("http://example.com/images/smartphone_x.jpg", product.getImageUrl());
        assertEquals(1, product.getTrendingDisplayOrder());
        assertEquals(5, product.getSellingDisplayOrder());

        // Test the getProductCategoryName helper method
        assertEquals("Electronics", product.getProductCategoryName());

        // Test toString (ensures Lombok's @Data generates a valid toString)
        assertNotNull(product.toString());

        // Test NoArgsConstructor and AllArgsConstructor
        ProductDetails defaultProduct = new ProductDetails();
        assertNotNull(defaultProduct);

        ProductDetails fullProduct = new ProductDetails(2L, "Laptop Pro", 50, new BigDecimal("1200.00"), category,
                true, "http://example.com/images/laptop_pro.jpg", 2, 3);
        assertEquals(2L, fullProduct.getProductDetailsId());
        assertEquals("Laptop Pro", fullProduct.getProductName());
        assertEquals(50, fullProduct.getProductQuantity());
        assertEquals(new BigDecimal("1200.00"), fullProduct.getPrice());
        assertEquals(category, fullProduct.getProductCategory());
        assertTrue(fullProduct.getIsActive());
        assertEquals("http://example.com/images/laptop_pro.jpg", fullProduct.getImageUrl());
        assertEquals(2, fullProduct.getTrendingDisplayOrder());
        assertEquals(3, fullProduct.getSellingDisplayOrder());

        // Test setters
        product.setProductName("Updated Smartphone");
        assertEquals("Updated Smartphone", product.getProductName());
        product.setPrice(new BigDecimal("650.00"));
        assertEquals(new BigDecimal("650.00"), product.getPrice());
        product.setImageUrl("http://example.com/images/updated_smartphone.jpg");
        assertEquals("http://example.com/images/updated_smartphone.jpg", product.getImageUrl());
        product.setTrendingDisplayOrder(3);
        assertEquals(3, product.getTrendingDisplayOrder());
        product.setSellingDisplayOrder(1);
        assertEquals(1, product.getSellingDisplayOrder());
    }

    @Test
    public void testProductCategoryNameWhenCategoryIsNull() {
        ProductDetails product = ProductDetails.builder()
                .productDetailsId(1L)
                .productName("Product without category")
                .productQuantity(10)
                .price(new BigDecimal("10.00"))
                .isActive(true)
                .productCategory(null) // Set product category to null
                .build();

        assertNull(product.getProductCategoryName());
    }
}