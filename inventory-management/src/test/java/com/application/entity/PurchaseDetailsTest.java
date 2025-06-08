package com.application.entity;

import org.junit.jupiter.api.Test;

import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

public class PurchaseDetailsTest {

    @Test
    public void testPurchaseDetailsEntity() {
        // Create test data for associated entities
        Date purchaseDate = new Date();

        AppUser user = new AppUser();
        user.setUserId(1L);
        user.setName("Alice Smith"); // Set user's name

        ProductDetails product = new ProductDetails();
        product.setProductDetailsId(101L);
        product.setProductName("Bluetooth Headphones"); // Set product's name

        // Create and test PurchaseDetails using the builder pattern
        PurchaseDetails purchase = PurchaseDetails.builder()
                .purchaseDetailsId(1L)
                .productCount(2)
                .purchaseDate(purchaseDate)
                .isActive(true)
                .productNameAtPurchase(product.getProductName()) // Set new field
                .userNameAtPurchase(user.getName()) // Set new field
                .user(user)
                .product(product)
                .build();

        // Verify all fields
        assertEquals(1L, purchase.getPurchaseDetailsId());
        assertEquals(2, purchase.getProductCount());
        assertEquals(purchaseDate, purchase.getPurchaseDate());
        assertTrue(purchase.getIsActive());
        assertEquals("Bluetooth Headphones", purchase.getProductNameAtPurchase()); // Verify new field
        assertEquals("Alice Smith", purchase.getUserNameAtPurchase()); // Verify new field
        assertEquals(user, purchase.getUser());
        assertEquals(product, purchase.getProduct());

        // Test Lombok generated getters and setters for all fields
        purchase.setProductCount(5);
        assertEquals(5, purchase.getProductCount());

        Date newPurchaseDate = new Date(System.currentTimeMillis() + 3600 * 1000); // 1 hour later
        purchase.setPurchaseDate(newPurchaseDate);
        assertEquals(newPurchaseDate, purchase.getPurchaseDate());

        purchase.setIsActive(false);
        assertFalse(purchase.getIsActive());

        purchase.setProductNameAtPurchase("Wireless Mouse (Updated)");
        assertEquals("Wireless Mouse (Updated)", purchase.getProductNameAtPurchase());

        purchase.setUserNameAtPurchase("Bob Johnson (Updated)");
        assertEquals("Bob Johnson (Updated)", purchase.getUserNameAtPurchase());

        // Test NoArgsConstructor and AllArgsConstructor
        PurchaseDetails defaultPurchase = new PurchaseDetails();
        assertNotNull(defaultPurchase);

        PurchaseDetails fullPurchase = new PurchaseDetails(
                2L,
                1,
                new Date(),
                true,
                "USB Drive",
                "Charlie Brown",
                user,
                product
        );
        assertEquals(2L, fullPurchase.getPurchaseDetailsId());
        assertEquals(1, fullPurchase.getProductCount());
        assertNotNull(fullPurchase.getPurchaseDate());
        assertTrue(fullPurchase.getIsActive());
        assertEquals("USB Drive", fullPurchase.getProductNameAtPurchase());
        assertEquals("Charlie Brown", fullPurchase.getUserNameAtPurchase());
        assertEquals(user, fullPurchase.getUser());
        assertEquals(product, fullPurchase.getProduct());

        // Test toString (ensures Lombok's @Data generates a valid toString)
        assertNotNull(purchase.toString());
        assertNotNull(fullPurchase.toString());
    }
}