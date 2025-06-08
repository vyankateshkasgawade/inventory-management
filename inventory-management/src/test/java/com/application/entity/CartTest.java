package com.application.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

public class CartTest {

    @Test
    public void testCartEntity() {
        // Create test data for associated entities
        Date addedDate = new Date();
        AppUser user = new AppUser();
        user.setUserId(10L); // Set an ID for completeness
        user.setName("John Doe"); // Set a name for the user

        ProductDetails product = new ProductDetails();
        product.setProductDetailsId(100L); // Set an ID for completeness
        product.setProductName("Laptop XYZ"); // Set product name
        product.setImageUrl("http://example.com/images/laptop.jpg"); // Set image URL
        product.setPrice(new BigDecimal("1200.00")); // Set product price for calculation

        // Create and test Cart using the builder pattern
        Cart cart = Cart.builder()
                .cartId(1L)
                .user(user)
                .product(product)
                .productName(product.getProductName()) // Set productName from ProductDetails
                .imageUrl(product.getImageUrl())       // Set imageUrl from ProductDetails
                .quantity(2)
                .addedDate(addedDate)
                .finalTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(2))) // Calculate total based on product price and quantity
                .isActive(true)
                .build();

        // Verify all fields
        assertEquals(1L, cart.getCartId());
        assertEquals(user, cart.getUser());
        assertEquals(product, cart.getProduct());
        assertEquals("Laptop XYZ", cart.getProductName()); // Verify new productName field
        assertEquals("http://example.com/images/laptop.jpg", cart.getImageUrl()); // Verify new imageUrl field
        assertEquals(2, cart.getQuantity());
        assertEquals(addedDate, cart.getAddedDate());
        assertEquals(new BigDecimal("2400.00"), cart.getFinalTotalAmount()); // Verify calculated total
        assertTrue(cart.getIsActive());

        // Test Lombok generated getters and setters
        // Update product name and image URL directly on the cart
        cart.setProductName("Updated Laptop ABC");
        assertEquals("Updated Laptop ABC", cart.getProductName());

        cart.setImageUrl("http://example.com/images/updated_laptop.jpg");
        assertEquals("http://example.com/images/updated_laptop.jpg", cart.getImageUrl());

        cart.setQuantity(3);
        assertEquals(3, cart.getQuantity());

        BigDecimal newTotal = new BigDecimal("3600.00");
        cart.setFinalTotalAmount(newTotal);
        assertEquals(newTotal, cart.getFinalTotalAmount());

        cart.setIsActive(false);
        assertFalse(cart.getIsActive());

        // Test NoArgsConstructor and AllArgsConstructor
        Cart defaultCart = new Cart();
        assertNotNull(defaultCart);

        Cart fullCart = new Cart(2L, user, product, "Gaming Mouse", "http://example.com/images/mouse.jpg",
                1, new Date(), new BigDecimal("50.00"), true);
        assertEquals(2L, fullCart.getCartId());
        assertEquals(user, fullCart.getUser());
        assertEquals(product, fullCart.getProduct());
        assertEquals("Gaming Mouse", fullCart.getProductName());
        assertEquals("http://example.com/images/mouse.jpg", fullCart.getImageUrl());
        assertEquals(1, fullCart.getQuantity());
        assertNotNull(fullCart.getAddedDate());
        assertEquals(new BigDecimal("50.00"), fullCart.getFinalTotalAmount());
        assertTrue(fullCart.getIsActive());

        // Test toString (ensures Lombok's @Data generates a valid toString)
        assertNotNull(cart.toString());
        assertNotNull(fullCart.toString());
    }
}