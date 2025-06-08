package com.application.dto;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RazorpayOrderResponseTest {

    private RazorpayOrderResponse response;

    @BeforeEach
    void setUp() {
        // Initialize a common RazorpayOrderResponse object for each test
        response = RazorpayOrderResponse.builder()
                .id("order_MDRFjF3x6A1xUe")
                .amount(50000.00) // Example amount in paise
                .currency("INR")
                .key("rzp_test_YourKeyId")
                .build();
    }

    @Test
    void testNoArgsConstructor() {
        RazorpayOrderResponse newResponse = new RazorpayOrderResponse();
        assertNotNull(newResponse);
        assertNull(newResponse.getId());
        assertEquals(0.0, newResponse.getAmount()); // Default for double is 0.0
        assertNull(newResponse.getCurrency());
        assertNull(newResponse.getKey());
    }

    @Test
    void testAllArgsConstructor() {
        RazorpayOrderResponse allArgsResponse = new RazorpayOrderResponse(
                "order_test_XYZ", 7500.00, "USD", "key_test_abcdef"
        );
        assertNotNull(allArgsResponse);
        assertEquals("order_test_XYZ", allArgsResponse.getId());
        assertEquals(7500.00, allArgsResponse.getAmount(), 0.001); // Use delta for double comparison
        assertEquals("USD", allArgsResponse.getCurrency());
        assertEquals("key_test_abcdef", allArgsResponse.getKey());
    }

    @Test
    void testBuilder() {
        assertNotNull(response);
        assertEquals("order_MDRFjF3x6A1xUe", response.getId());
        assertEquals(50000.00, response.getAmount(), 0.001);
        assertEquals("INR", response.getCurrency());
        assertEquals("rzp_test_YourKeyId", response.getKey());
    }

    @Test
    void testGettersAndSetters() {
        RazorpayOrderResponse testResponse = new RazorpayOrderResponse();

        testResponse.setId("order_newId");
        assertEquals("order_newId", testResponse.getId());

        testResponse.setAmount(12345.67);
        assertEquals(12345.67, testResponse.getAmount(), 0.001);

        testResponse.setCurrency("EUR");
        assertEquals("EUR", testResponse.getCurrency());

        testResponse.setKey("new_test_key");
        assertEquals("new_test_key", testResponse.getKey());
    }

    @Test
    void testEqualsAndHashCode() {
        // Create an identical DTO
        RazorpayOrderResponse sameResponse = RazorpayOrderResponse.builder()
                .id("order_MDRFjF3x6A1xUe")
                .amount(50000.00)
                .currency("INR")
                .key("rzp_test_YourKeyId")
                .build();

        // Create a DTO with a different ID
        RazorpayOrderResponse differentIdResponse = RazorpayOrderResponse.builder()
                .id("order_DIFFERENT")
                .amount(50000.00)
                .currency("INR")
                .key("rzp_test_YourKeyId")
                .build();

        // Create a DTO with a different amount
        RazorpayOrderResponse differentAmountResponse = RazorpayOrderResponse.builder()
                .id("order_MDRFjF3x6A1xUe")
                .amount(60000.00)
                .currency("INR")
                .key("rzp_test_YourKeyId")
                .build();

        // Test equality
        assertEquals(response, sameResponse);
        assertEquals(response.hashCode(), sameResponse.hashCode());

        // Test inequality based on ID
        assertNotEquals(response, differentIdResponse);
        assertNotEquals(response.hashCode(), differentIdResponse.hashCode());

        // Test inequality based on amount
        assertNotEquals(response, differentAmountResponse);
        assertNotEquals(response.hashCode(), differentAmountResponse.hashCode());

        // Test with nulls and different object types
        assertFalse(response.equals(null));
        assertFalse(response.equals(new Object()));
    }

    @Test
    void testToString() {
        // Just assert that toString() doesn't throw an error and contains the relevant data
        String dtoString = response.toString();
        assertNotNull(dtoString);
        assertTrue(dtoString.contains("id=order_MDRFjF3x6A1xUe"));
        assertTrue(dtoString.contains("amount=50000.0")); // Note: double might append .0
        assertTrue(dtoString.contains("currency=INR"));
        assertTrue(dtoString.contains("key=rzp_test_YourKeyId"));
    }
}