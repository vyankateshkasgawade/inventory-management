package com.application.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RegistrationRequestTest {

    private RegistrationRequest request;
    private AddressDTO testAddressDTO;

    @BeforeEach
    void setUp() {
        // Initialize an AddressDTO to be used within the RegistrationRequest
        testAddressDTO = AddressDTO.builder()
                .countryRegion("India")
                .pincode("411001")
                .flatHouseNoBuildingCompanyApartment("101, A Block")
                .areaStreetSectorVillage("Main Street")
                .landmark("Near City Center")
                .townCity("Pune")
                .state("Maharashtra")
                .build();

        // Initialize a common RegistrationRequest object for each test
        request = RegistrationRequest.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .password("securePass123")
                .otp("654321")
                .phone("9876543210")
                .dob("1990-01-15")
                .role("USER")
                .address(testAddressDTO)
                .build();
    }

    @Test
    void testNoArgsConstructor() {
        RegistrationRequest newRequest = new RegistrationRequest();
        assertNotNull(newRequest);
        assertNull(newRequest.getName());
        assertNull(newRequest.getEmail());
        assertNull(newRequest.getPassword());
        assertNull(newRequest.getOtp());
        assertNull(newRequest.getPhone());
        assertNull(newRequest.getDob());
        assertNull(newRequest.getRole());
        assertNull(newRequest.getAddress());
    }

    @Test
    void testAllArgsConstructor() {
        AddressDTO anotherAddressDTO = AddressDTO.builder()
                .countryRegion("USA")
                .pincode("90210")
                .build();

        RegistrationRequest allArgsRequest = new RegistrationRequest(
                "Jane Smith",
                "jane.smith@example.com",
                "anotherSecurePass",
                "123456",
                "0987654321",
                "1985-07-20",
                "ADMIN",
                anotherAddressDTO
        );

        assertNotNull(allArgsRequest);
        assertEquals("Jane Smith", allArgsRequest.getName());
        assertEquals("jane.smith@example.com", allArgsRequest.getEmail());
        assertEquals("anotherSecurePass", allArgsRequest.getPassword());
        assertEquals("123456", allArgsRequest.getOtp());
        assertEquals("0987654321", allArgsRequest.getPhone());
        assertEquals("1985-07-20", allArgsRequest.getDob());
        assertEquals("ADMIN", allArgsRequest.getRole());
        assertEquals(anotherAddressDTO, allArgsRequest.getAddress());
    }

    @Test
    void testBuilder() {
        assertNotNull(request);
        assertEquals("John Doe", request.getName());
        assertEquals("john.doe@example.com", request.getEmail());
        assertEquals("securePass123", request.getPassword());
        assertEquals("654321", request.getOtp());
        assertEquals("9876543210", request.getPhone());
        assertEquals("1990-01-15", request.getDob());
        assertEquals("USER", request.getRole());
        assertEquals(testAddressDTO, request.getAddress()); // Verify the AddressDTO object
    }

    @Test
    void testGettersAndSetters() {
        RegistrationRequest testRequest = new RegistrationRequest();

        testRequest.setName("New Name");
        assertEquals("New Name", testRequest.getName());

        testRequest.setEmail("new.email@example.com");
        assertEquals("new.email@example.com", testRequest.getEmail());

        testRequest.setPassword("newPass");
        assertEquals("newPass", testRequest.getPassword());

        testRequest.setOtp("999999");
        assertEquals("999999", testRequest.getOtp());

        testRequest.setPhone("1122334455");
        assertEquals("1122334455", testRequest.getPhone());

        testRequest.setDob("2000-03-25");
        assertEquals("2000-03-25", testRequest.getDob());

        testRequest.setRole("GUEST");
        assertEquals("GUEST", testRequest.getRole());

        AddressDTO newAddressDTO = AddressDTO.builder().countryRegion("Canada").pincode("V6B 1P1").build();
        testRequest.setAddress(newAddressDTO);
        assertEquals(newAddressDTO, testRequest.getAddress());
    }

    @Test
    void testEqualsAndHashCode() {
        // Create an identical DTO
        RegistrationRequest sameRequest = RegistrationRequest.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .password("securePass123")
                .otp("654321")
                .phone("9876543210")
                .dob("1990-01-15")
                .role("USER")
                .address(testAddressDTO) // Same address object
                .build();

        // Create a DTO with a different email (should cause inequality)
        RegistrationRequest differentEmailRequest = RegistrationRequest.builder()
                .name("John Doe")
                .email("diff.email@example.com") // Different email
                .password("securePass123")
                .otp("654321")
                .phone("9876543210")
                .dob("1990-01-15")
                .role("USER")
                .address(testAddressDTO)
                .build();

        // Create a DTO with a different address object
        AddressDTO anotherAddressDTO = AddressDTO.builder()
                .countryRegion("UK")
                .pincode("SW1A 0AA")
                .build();
        RegistrationRequest differentAddressRequest = RegistrationRequest.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .password("securePass123")
                .otp("654321")
                .phone("9876543210")
                .dob("1990-01-15")
                .role("USER")
                .address(anotherAddressDTO) // Different address object
                .build();

        // Test equality
        assertEquals(request, sameRequest);
        assertEquals(request.hashCode(), sameRequest.hashCode());

        // Test inequality
        assertNotEquals(request, differentEmailRequest);
        assertNotEquals(request.hashCode(), differentEmailRequest.hashCode());

        assertNotEquals(request, differentAddressRequest);
        assertNotEquals(request.hashCode(), differentAddressRequest.hashCode());

        // Test with null address
        RegistrationRequest nullAddressRequest = RegistrationRequest.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .password("securePass123")
                .otp("654321")
                .phone("9876543210")
                .dob("1990-01-15")
                .role("USER")
                .address(null) // Null address
                .build();
        assertNotEquals(request, nullAddressRequest);
        
        // Test with nulls and different object types
        assertFalse(request.equals(null));
        assertFalse(request.equals(new Object()));
    }

    @Test
    void testToString() {
        // Just assert that toString() doesn't throw an error and contains the relevant data.
        // Lombok's @Data on AddressDTO handles its toString(), so no circularity here.
        String dtoString = request.toString();
        assertNotNull(dtoString);
        assertTrue(dtoString.contains("name=John Doe"));
        assertTrue(dtoString.contains("email=john.doe@example.com"));
        assertTrue(dtoString.contains("phone=9876543210"));
        assertTrue(dtoString.contains("dob=1990-01-15"));
        assertTrue(dtoString.contains("role=USER"));
        assertTrue(dtoString.contains("address=")); // Checks if address field is present in string
        assertTrue(dtoString.contains("countryRegion=India")); // Checks if AddressDTO content is in string
    }
}