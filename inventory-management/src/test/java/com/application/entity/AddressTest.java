package com.application.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AddressTest {

    private Address address;
    private AppUser appUser; // Assuming AppUser exists and has an address field

    @BeforeEach
    void setUp() {
        // Initialize a common AppUser for tests if needed
        appUser = AppUser.builder()
                .userId(1L)
                .name("testUser")
                .email("test@example.com")
                .password("password")
                .build();

        // Initialize a common Address object for each test
        address = Address.builder()
                .addressId(1L)
                .countryRegion("India")
                .pincode("411001")
                .flatHouseNoBuildingCompanyApartment("101, A Block")
                .areaStreetSectorVillage("Main Street")
                .landmark("Near City Center")
                .townCity("Pune")
                .state("Maharashtra")
                .user(appUser) // Set the user for testing the relationship
                .build();

        // Establish bidirectional relationship if AppUser also has a setter for address
        // (This would typically be done in AppUser.setAddress() or similar)
        if (appUser != null) {
            appUser.setAddress(address);
        }
    }

    @Test
    void testNoArgsConstructor() {
        Address newAddress = new Address();
        assertNotNull(newAddress);
        assertNull(newAddress.getAddressId()); // Should be null before persistence
    }

    @Test
    void testAllArgsConstructor() {
        Address allArgsAddress = new Address(
                2L, "USA", "90210", "Suite 500",
                "Beverly Hills Blvd", "Hollywood Sign", "Los Angeles",
                "California", null // No user for this test
        );

        assertNotNull(allArgsAddress);
        assertEquals(2L, allArgsAddress.getAddressId());
        assertEquals("USA", allArgsAddress.getCountryRegion());
        assertEquals("90210", allArgsAddress.getPincode());
        assertEquals("Suite 500", allArgsAddress.getFlatHouseNoBuildingCompanyApartment());
        assertEquals("Beverly Hills Blvd", allArgsAddress.getAreaStreetSectorVillage());
        assertEquals("Hollywood Sign", allArgsAddress.getLandmark());
        assertEquals("Los Angeles", allArgsAddress.getTownCity());
        assertEquals("California", allArgsAddress.getState());
        assertNull(allArgsAddress.getUser());
    }

    @Test
    void testBuilder() {
        assertNotNull(address);
        assertEquals(1L, address.getAddressId());
        assertEquals("India", address.getCountryRegion());
        assertEquals("411001", address.getPincode());
        assertEquals("101, A Block", address.getFlatHouseNoBuildingCompanyApartment());
        assertEquals("Main Street", address.getAreaStreetSectorVillage());
        assertEquals("Near City Center", address.getLandmark());
        assertEquals("Pune", address.getTownCity());
        assertEquals("Maharashtra", address.getState());
        assertEquals(appUser, address.getUser()); // Verify the user object
    }

    @Test
    void testGettersAndSetters() {
        Address testAddress = new Address();
        testAddress.setAddressId(10L);
        assertEquals(10L, testAddress.getAddressId());

        testAddress.setCountryRegion("Canada");
        assertEquals("Canada", testAddress.getCountryRegion());

        testAddress.setPincode("V6B 1P1");
        assertEquals("V6B 1P1", testAddress.getPincode());

        testAddress.setFlatHouseNoBuildingCompanyApartment("Unit 5");
        assertEquals("Unit 5", testAddress.getFlatHouseNoBuildingCompanyApartment());

        testAddress.setAreaStreetSectorVillage("Robson Street");
        assertEquals("Robson Street", testAddress.getAreaStreetSectorVillage());

        testAddress.setLandmark("Near Stanley Park");
        assertEquals("Near Stanley Park", testAddress.getLandmark());

        testAddress.setTownCity("Vancouver");
        assertEquals("Vancouver", testAddress.getTownCity());

        testAddress.setState("British Columbia");
        assertEquals("British Columbia", testAddress.getState());

        AppUser newUser = AppUser.builder().userId(2L).name("newUser").build();
        testAddress.setUser(newUser);
        assertEquals(newUser, testAddress.getUser());
    }

    @Test
    void testEqualsAndHashCode() {
        Address sameAddress = Address.builder()
                .addressId(1L)
                .countryRegion("India")
                .pincode("411001")
                .flatHouseNoBuildingCompanyApartment("101, A Block")
                .areaStreetSectorVillage("Main Street")
                .landmark("Near City Center")
                .townCity("Pune")
                .state("Maharashtra")
                .user(appUser)
                .build();

        Address differentAddressId = Address.builder()
                .addressId(2L) // Different ID
                .countryRegion("India")
                .pincode("411001")
                .build();

        Address differentDetails = Address.builder()
                .addressId(1L)
                .countryRegion("USA") // Different detail
                .pincode("411001")
                .build();

        // Test equality based on all fields (Lombok's @Data default)
        assertEquals(address, sameAddress);

    }


}