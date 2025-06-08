package com.application.dto;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProductOrderUpdateDTOTest {

    private ProductOrderUpdateDTO productOrderUpdateDTO;

    @BeforeEach
    void setUp() {
        // Initialize a common ProductOrderUpdateDTO object for each test
        productOrderUpdateDTO = new ProductOrderUpdateDTO(101L, 1);
    }

    @Test
    void testNoArgsConstructor() {
        ProductOrderUpdateDTO newDTO = new ProductOrderUpdateDTO();
        assertNotNull(newDTO);
        assertNull(newDTO.getId());
        assertNull(newDTO.getDisplayOrder());
    }

    @Test
    void testAllArgsConstructor() {
        ProductOrderUpdateDTO allArgsDTO = new ProductOrderUpdateDTO(202L, 5);
        assertNotNull(allArgsDTO);
        assertEquals(202L, allArgsDTO.getId());
        assertEquals(5, allArgsDTO.getDisplayOrder());
    }

    // Note: ProductOrderUpdateDTO does not have @Builder annotation in your provided code snippet.
    // If you add @Builder later, you would include a testBuilder() method.
    // @Test
    // void testBuilder() {
    //     ProductOrderUpdateDTO builderDTO = ProductOrderUpdateDTO.builder()
    //             .id(303L)
    //             .displayOrder(10)
    //             .build();
    //     assertNotNull(builderDTO);
    //     assertEquals(303L, builderDTO.getId());
    //     assertEquals(10, builderDTO.getDisplayOrder());
    // }

    @Test
    void testGettersAndSetters() {
        ProductOrderUpdateDTO testDTO = new ProductOrderUpdateDTO();

        testDTO.setId(99L);
        assertEquals(99L, testDTO.getId());

        testDTO.setDisplayOrder(0); // Set to 0, which is a valid integer value
        assertEquals(0, testDTO.getDisplayOrder());

        testDTO.setDisplayOrder(null); // Test setting to null as per DTO comment
        assertNull(testDTO.getDisplayOrder());
    }

    @Test
    void testEqualsAndHashCode() {
        // Create an identical DTO
        ProductOrderUpdateDTO sameDTO = new ProductOrderUpdateDTO(101L, 1);

        // Create a DTO with a different ID
        ProductOrderUpdateDTO differentIdDTO = new ProductOrderUpdateDTO(102L, 1);

        // Create a DTO with a different displayOrder
        ProductOrderUpdateDTO differentDisplayOrderDTO = new ProductOrderUpdateDTO(101L, 2);

        // Test equality
        assertEquals(productOrderUpdateDTO, sameDTO);
        assertEquals(productOrderUpdateDTO.hashCode(), sameDTO.hashCode());

        // Test inequality based on ID
        assertNotEquals(productOrderUpdateDTO, differentIdDTO);
        assertNotEquals(productOrderUpdateDTO.hashCode(), differentIdDTO.hashCode());

        // Test inequality based on displayOrder
        assertNotEquals(productOrderUpdateDTO, differentDisplayOrderDTO);
        assertNotEquals(productOrderUpdateDTO.hashCode(), differentDisplayOrderDTO.hashCode());

        // Test with null displayOrder
        ProductOrderUpdateDTO nullDisplayOrderDTO = new ProductOrderUpdateDTO(101L, null);
        assertNotEquals(productOrderUpdateDTO, nullDisplayOrderDTO); // 1 vs null
        assertEquals(new ProductOrderUpdateDTO(101L, null), nullDisplayOrderDTO); // null vs null

        // Test with null ID
        ProductOrderUpdateDTO nullIdDTO = new ProductOrderUpdateDTO(null, 1);
        assertNotEquals(productOrderUpdateDTO, nullIdDTO);

        ProductOrderUpdateDTO allNullDTO = new ProductOrderUpdateDTO(null, null);
        assertEquals(new ProductOrderUpdateDTO(null, null), allNullDTO);


        // Test with nulls and different object types
        assertFalse(productOrderUpdateDTO.equals(null));
        assertFalse(productOrderUpdateDTO.equals(new Object()));
    }

    @Test
    void testToString() {
        // Just assert that toString() doesn't throw an error and contains the relevant data
        String dtoString = productOrderUpdateDTO.toString();
        assertNotNull(dtoString);
        assertTrue(dtoString.contains("id=101"));
        assertTrue(dtoString.contains("displayOrder=1"));

        // Test toString with null displayOrder
        ProductOrderUpdateDTO dtoWithNullDisplayOrder = new ProductOrderUpdateDTO(500L, null);
        String nullDisplayOrderString = dtoWithNullDisplayOrder.toString();
        assertNotNull(nullDisplayOrderString);
        assertTrue(nullDisplayOrderString.contains("id=500"));
        assertTrue(nullDisplayOrderString.contains("displayOrder=null"));
    }
}