package com.application.dto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RazorpayOrderRequestTest {

    private RazorpayOrderRequest request;

    @BeforeEach
    void setUp() {
        // Initialize a common RazorpayOrderRequest object for each test
        request = RazorpayOrderRequest.builder()
                .amount(100.00) // Example amount
                .currency("INR")
                .build();
    }

    @Test
    void testNoArgsConstructor() {
        RazorpayOrderRequest newRequest = new RazorpayOrderRequest();
        assertNotNull(newRequest);
        assertEquals(0.0, newRequest.getAmount()); // Default for double is 0.0
        assertNull(newRequest.getCurrency());
    }

    @Test
    void testAllArgsConstructor() {
        RazorpayOrderRequest allArgsRequest = new RazorpayOrderRequest(250.50, "USD");
        assertNotNull(allArgsRequest);
        assertEquals(250.50, allArgsRequest.getAmount(), 0.001); // Use delta for double comparison
        assertEquals("USD", allArgsRequest.getCurrency());
    }

    @Test
    void testBuilder() {
        assertNotNull(request);
        assertEquals(100.00, request.getAmount(), 0.001); // Use delta for double comparison
        assertEquals("INR", request.getCurrency());
    }

    @Test
    void testGettersAndSetters() {
        RazorpayOrderRequest testRequest = new RazorpayOrderRequest();

        testRequest.setAmount(500.75);
        assertEquals(500.75, testRequest.getAmount(), 0.001);

        testRequest.setCurrency("EUR");
        assertEquals("EUR", testRequest.getCurrency());
    }

    @Test
    void testEqualsAndHashCode() {
        // Create an identical DTO
        RazorpayOrderRequest sameRequest = RazorpayOrderRequest.builder()
                .amount(100.00)
                .currency("INR")
                .build();

        // Create a DTO with a different amount
        RazorpayOrderRequest differentAmountRequest = RazorpayOrderRequest.builder()
                .amount(150.00)
                .currency("INR")
                .build();

        // Create a DTO with a different currency
        RazorpayOrderRequest differentCurrencyRequest = RazorpayOrderRequest.builder()
                .amount(100.00)
                .currency("USD")
                .build();

        // Test equality
        assertEquals(request, sameRequest);
        assertEquals(request.hashCode(), sameRequest.hashCode());

        // Test inequality based on amount
        assertNotEquals(request, differentAmountRequest);
        assertNotEquals(request.hashCode(), differentAmountRequest.hashCode());

        // Test inequality based on currency
        assertNotEquals(request, differentCurrencyRequest);
        assertNotEquals(request.hashCode(), differentCurrencyRequest.hashCode());

        // Test with null currency
        RazorpayOrderRequest nullCurrency = RazorpayOrderRequest.builder().amount(100.00).currency(null).build();
        assertNotEquals(request, nullCurrency);

        // Test with nulls and different object types
        assertFalse(request.equals(null));
        assertFalse(request.equals(new Object()));
    }

    @Test
    void testToString() {
        // Just assert that toString() doesn't throw an error and contains the relevant data
        String dtoString = request.toString();
        assertNotNull(dtoString);
        assertTrue(dtoString.contains("amount=100.0")); // Note: double might append .0
        assertTrue(dtoString.contains("currency=INR"));
    }
}