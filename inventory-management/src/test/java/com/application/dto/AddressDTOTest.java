package com.application.dto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AddressDTOTest {

    private AddressDTO addressDTO;

    @BeforeEach
    void setUp() {
        // Initialize a common AddressDTO object for each test
        addressDTO = AddressDTO.builder()
                .countryRegion("India")
                .pincode("411001")
                .flatHouseNoBuildingCompanyApartment("101, A Block")
                .areaStreetSectorVillage("Main Street")
                .landmark("Near City Center")
                .townCity("Pune")
                .state("Maharashtra")
                .build();
    }

    @Test
    void testNoArgsConstructor() {
        AddressDTO newAddressDTO = new AddressDTO();
        assertNotNull(newAddressDTO);
        assertNull(newAddressDTO.getCountryRegion());
        assertNull(newAddressDTO.getPincode());
        // Assert other fields are null if not explicitly set
    }

    @Test
    void testAllArgsConstructor() {
        AddressDTO allArgsAddressDTO = new AddressDTO(
                "USA", "90210", "Suite 500", "Beverly Hills Blvd",
                "Hollywood Sign", "Los Angeles", "California"
        );

        assertNotNull(allArgsAddressDTO);
        assertEquals("USA", allArgsAddressDTO.getCountryRegion());
        assertEquals("90210", allArgsAddressDTO.getPincode());
        assertEquals("Suite 500", allArgsAddressDTO.getFlatHouseNoBuildingCompanyApartment());
        assertEquals("Beverly Hills Blvd", allArgsAddressDTO.getAreaStreetSectorVillage());
        assertEquals("Hollywood Sign", allArgsAddressDTO.getLandmark());
        assertEquals("Los Angeles", allArgsAddressDTO.getTownCity());
        assertEquals("California", allArgsAddressDTO.getState());
    }

    @Test
    void testBuilder() {
        assertNotNull(addressDTO);
        assertEquals("India", addressDTO.getCountryRegion());
        assertEquals("411001", addressDTO.getPincode());
        assertEquals("101, A Block", addressDTO.getFlatHouseNoBuildingCompanyApartment());
        assertEquals("Main Street", addressDTO.getAreaStreetSectorVillage());
        assertEquals("Near City Center", addressDTO.getLandmark());
        assertEquals("Pune", addressDTO.getTownCity());
        assertEquals("Maharashtra", addressDTO.getState());
    }

    @Test
    void testGettersAndSetters() {
        AddressDTO testAddressDTO = new AddressDTO();

        testAddressDTO.setCountryRegion("Canada");
        assertEquals("Canada", testAddressDTO.getCountryRegion());

        testAddressDTO.setPincode("V6B 1P1");
        assertEquals("V6B 1P1", testAddressDTO.getPincode());

        testAddressDTO.setFlatHouseNoBuildingCompanyApartment("Unit 5");
        assertEquals("Unit 5", testAddressDTO.getFlatHouseNoBuildingCompanyApartment());

        testAddressDTO.setAreaStreetSectorVillage("Robson Street");
        assertEquals("Robson Street", testAddressDTO.getAreaStreetSectorVillage());

        testAddressDTO.setLandmark("Near Stanley Park");
        assertEquals("Near Stanley Park", testAddressDTO.getLandmark());

        testAddressDTO.setTownCity("Vancouver");
        assertEquals("Vancouver", testAddressDTO.getTownCity());

        testAddressDTO.setState("British Columbia");
        assertEquals("British Columbia", testAddressDTO.getState());
    }

    @Test
    void testEqualsAndHashCode() {
        // Create an identical DTO
        AddressDTO sameAddressDTO = AddressDTO.builder()
                .countryRegion("India")
                .pincode("411001")
                .flatHouseNoBuildingCompanyApartment("101, A Block")
                .areaStreetSectorVillage("Main Street")
                .landmark("Near City Center")
                .townCity("Pune")
                .state("Maharashtra")
                .build();

        // Create a DTO with a different value
        AddressDTO differentAddressDTO = AddressDTO.builder()
                .countryRegion("India")
                .pincode("400001") // Different pincode
                .flatHouseNoBuildingCompanyApartment("101, A Block")
                .areaStreetSectorVillage("Main Street")
                .landmark("Near City Center")
                .townCity("Pune")
                .state("Maharashtra")
                .build();

        // Test equality
        assertEquals(addressDTO, sameAddressDTO);
        assertEquals(addressDTO.hashCode(), sameAddressDTO.hashCode());

        // Test inequality
        assertNotEquals(addressDTO, differentAddressDTO);
        assertNotEquals(addressDTO.hashCode(), differentAddressDTO.hashCode());

        // Test with nulls and different object types
        assertFalse(addressDTO.equals(null));
        assertFalse(addressDTO.equals(new Object()));
    }

    @Test
    void testToString() {
        // Just assert that toString() doesn't throw an error and contains some relevant data
        String dtoString = addressDTO.toString();
        assertNotNull(dtoString);
        assertTrue(dtoString.contains("countryRegion=India"));
        assertTrue(dtoString.contains("pincode=411001"));
        assertTrue(dtoString.contains("townCity=Pune"));
    }
}
