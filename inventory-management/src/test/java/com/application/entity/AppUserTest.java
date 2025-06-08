package com.application.entity;

import org.junit.jupiter.api.Test;

import com.application.entity.AppUser;
import com.application.entity.Cart;
import com.application.entity.PurchaseDetails;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class AppUserTest {

    @Test
    public void testAppUserEntity() {
        // Create test data
        LocalDate dob = LocalDate.of(1990, 5, 15);
        List<PurchaseDetails> purchases = new ArrayList<>();
        List<Cart> cartItems = new ArrayList<>();

        // Create and test AppUser
        AppUser user = AppUser.builder()
                .userId(1L)
                .name("John Doe")
                .email("john@example.com")
                .dob("1990/5/15")
                .phone("1234567890")
                .password("password123")
                .isActive(true)
                .role("USER")
                .purchaseDetails(purchases)
                .cartItems(cartItems)
                .build();

    }
}