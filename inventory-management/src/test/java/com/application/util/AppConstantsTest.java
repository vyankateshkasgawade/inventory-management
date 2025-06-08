package com.application.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppConstantsTest {

    @Test
    void testGeneralConstants() {
        assertEquals("Success", AppConstants.SUCCESS);
        assertEquals("Failed", AppConstants.FAILED);
        assertEquals("Something went wrong.", AppConstants.SOMETHING_WENT_WRONG);
    }

    @Test
    void testProductCategoryConstants() {
        assertEquals("Product Category not found with id: ", AppConstants.CATEGORY_NOT_FOUND);
        assertEquals("Product Category deleted successfully.", AppConstants.CATEGORY_DELETED);
    }

    @Test
    void testProductDetailsConstants() {
        assertEquals("Product not found with id: ", AppConstants.PRODUCT_NOT_FOUND);
        assertEquals("Product deleted successfully.", AppConstants.PRODUCT_DELETED);
    }

    @Test
    void testAppUserConstants() {
        assertEquals("User not found with id: ", AppConstants.USER_NOT_FOUND);
        assertEquals("User registered successfully.", AppConstants.USER_REGISTERED);
        assertEquals("User deleted successfully.", AppConstants.USER_DELETED);
        assertEquals("User with given details already exists.", AppConstants.USER_ALREADY_EXISTS);
        assertEquals("Email Already Exist", AppConstants.EMAIL_ALREADY_EXISTS); // Added this based on your class
    }

    @Test
    void testCartConstants() {
        assertEquals("Cart item not found with id: ", AppConstants.CART_NOT_FOUND);
        assertEquals("Cart item deleted successfully.", AppConstants.CART_DELETED);
    }

    @Test
    void testPurchaseDetailsConstants() {
        assertEquals("Purchase not found with id: ", AppConstants.PURCHASE_NOT_FOUND);
        assertEquals("Purchase deleted successfully.", AppConstants.PURCHASE_DELETED);
        // Test the format string for PURCHASE_ALREADY_EXISTS
        String formattedMessage = String.format(AppConstants.PURCHASE_ALREADY_EXISTS, 1L, 101L, "2023-01-01");
        assertTrue(formattedMessage.contains("Purchase already exists for user ID: 1, product ID: 101 on 2023-01-01"));
    }

    @Test
    void testLoggingTags() {
        assertEquals("[SERVICE] ", AppConstants.SERVICE_LOG_PREFIX);
        assertEquals("[CONTROLLER] ", AppConstants.CONTROLLER_LOG_PREFIX);
        assertEquals("[REPOSITORY] ", AppConstants.REPOSITORY_LOG_PREFIX);
    }

    @Test
    void testAllConstantsAreNotNull() {
        // This test ensures that no constant is accidentally set to null
        assertNotNull(AppConstants.SUCCESS);
        assertNotNull(AppConstants.FAILED);
        assertNotNull(AppConstants.SOMETHING_WENT_WRONG);
        assertNotNull(AppConstants.CATEGORY_NOT_FOUND);
        assertNotNull(AppConstants.CATEGORY_DELETED);
        assertNotNull(AppConstants.PRODUCT_NOT_FOUND);
        assertNotNull(AppConstants.PRODUCT_DELETED);
        assertNotNull(AppConstants.USER_NOT_FOUND);
        assertNotNull(AppConstants.USER_REGISTERED);
        assertNotNull(AppConstants.USER_DELETED);
        assertNotNull(AppConstants.USER_ALREADY_EXISTS);
        assertNotNull(AppConstants.CART_NOT_FOUND);
        assertNotNull(AppConstants.CART_DELETED);
        assertNotNull(AppConstants.PURCHASE_NOT_FOUND);
        assertNotNull(AppConstants.PURCHASE_DELETED);
        assertNotNull(AppConstants.PURCHASE_ALREADY_EXISTS);
        assertNotNull(AppConstants.SERVICE_LOG_PREFIX);
        assertNotNull(AppConstants.CONTROLLER_LOG_PREFIX);
        assertNotNull(AppConstants.REPOSITORY_LOG_PREFIX);
        assertNotNull(AppConstants.EMAIL_ALREADY_EXISTS);
    }
}