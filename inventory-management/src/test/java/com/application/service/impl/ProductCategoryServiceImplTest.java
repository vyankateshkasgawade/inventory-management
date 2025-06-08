package com.application.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;
import java.util.Optional;
import com.application.dto.ProductCategoryDTO;
import com.application.entity.ProductCategory;
import com.application.exception.ProductCategoryException;
import com.application.exception.ResourceNotFoundException;
import com.application.repository.ProductCategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductCategoryServiceImplTest {

    @InjectMocks
    private ProductCategoryServiceImpl productCategoryService;

    @Mock
    private ProductCategoryRepository productCategoryRepository;

    // -------------------------------------------- test Create Category -----------------------------------------------------------------------------------

    @Test
    void testCreateCategory_Success() {
        ProductCategoryDTO dto = new ProductCategoryDTO(null, "Mobile", true);
        when(productCategoryRepository.findByProductCategoryNameAndIsActiveTrue("Mobile")).thenReturn(Optional.empty());

        ProductCategory savedEntity = ProductCategory.builder()
                .productCategoryId(1L)
                .productCategoryName("Mobile")
                .isActive(true)
                .build(); // Removed productDetails as it's not relevant for this mapping

        when(productCategoryRepository.save(any(ProductCategory.class))).thenReturn(savedEntity);

        ProductCategoryDTO result = productCategoryService.createCategory(dto);

        assertEquals("Mobile", result.getProductCategoryName());
        assertTrue(result.getIsActive());
        verify(productCategoryRepository).findByProductCategoryNameAndIsActiveTrue("Mobile");
        verify(productCategoryRepository).save(any(ProductCategory.class));
    }

    @Test
    void testCreateCategory_AlreadyExists() {
        ProductCategoryDTO dto = new ProductCategoryDTO(null, "Mobile", true);

        ProductCategory existing = ProductCategory.builder()
                .productCategoryId(1L)
                .productCategoryName("Mobile")
                .isActive(true)
                .build(); // Removed productDetails

        when(productCategoryRepository.findByProductCategoryNameAndIsActiveTrue("Mobile")).thenReturn(Optional.of(existing));

        ProductCategoryException exception = assertThrows(ProductCategoryException.class, () -> productCategoryService.createCategory(dto));
        assertTrue(exception.getMessage().contains("Product Category with name 'Mobile' already exists."));
        verify(productCategoryRepository).findByProductCategoryNameAndIsActiveTrue("Mobile");
        verify(productCategoryRepository, never()).save(any(ProductCategory.class));
    }

    @Test
    void testCreateCategory_ThrowsIllegalArgumentException() {
        ProductCategoryDTO dto = new ProductCategoryDTO(null, "Electronics", true);
        when(productCategoryRepository.findByProductCategoryNameAndIsActiveTrue("Electronics")).thenReturn(Optional.empty());

        when(productCategoryRepository.save(any(ProductCategory.class)))
                .thenThrow(new IllegalArgumentException("Invalid category data"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> productCategoryService.createCategory(dto));

        assertEquals("Invalid category data", exception.getMessage());
        verify(productCategoryRepository).findByProductCategoryNameAndIsActiveTrue("Electronics");
        verify(productCategoryRepository).save(any(ProductCategory.class));
    }

    @Test
    void testCreateCategory_ThrowsResourceNotFoundException() {
        ProductCategoryDTO dto = new ProductCategoryDTO(null, "Books", true);
        when(productCategoryRepository.findByProductCategoryNameAndIsActiveTrue("Books")).thenReturn(Optional.empty());

        // Simulate a scenario where a related resource is not found during save
        // (though less common for a simple category save, the catch block exists)
        doThrow(new ResourceNotFoundException("Related resource not found"))
                .when(productCategoryRepository).save(any(ProductCategory.class));

        assertThrows(ResourceNotFoundException.class, () -> productCategoryService.createCategory(dto));
        verify(productCategoryRepository).findByProductCategoryNameAndIsActiveTrue("Books");
        verify(productCategoryRepository).save(any(ProductCategory.class));
    }

    @Test
    void testCreateCategory_ThrowsGenericException() {
        ProductCategoryDTO dto = new ProductCategoryDTO(null, "Furniture", true);
        when(productCategoryRepository.findByProductCategoryNameAndIsActiveTrue("Furniture")).thenReturn(Optional.empty());

        when(productCategoryRepository.save(any(ProductCategory.class)))
                .thenThrow(new NullPointerException("Unexpected null"));

        ProductCategoryException ex = assertThrows(ProductCategoryException.class,
                () -> productCategoryService.createCategory(dto));

        assertTrue(ex.getMessage().contains("Something went wrong"));
        assertTrue(ex.getCause() instanceof NullPointerException);
        verify(productCategoryRepository).findByProductCategoryNameAndIsActiveTrue("Furniture");
        verify(productCategoryRepository).save(any(ProductCategory.class));
    }


    //-------------------------------------------- test Get Category By Id-----------------------------------------------------------------------------------
    @Test
    void testGetCategoryById_Success() {
        ProductCategory category = ProductCategory.builder()
                .productCategoryId(1L)
                .productCategoryName("Electronics")
                .isActive(true)
                .build(); // Removed productDetails as it's not in mapToDTO

        when(productCategoryRepository.findById(1L)).thenReturn(Optional.of(category));

        ProductCategoryDTO result = productCategoryService.getCategoryByCategoryId(1L);

        assertEquals("Electronics", result.getProductCategoryName());
        assertTrue(result.getIsActive());
        verify(productCategoryRepository).findById(1L);
    }


    // Negative: getCategoryById - inactive
    @Test
    void testGetCategoryById_Inactive() {
        ProductCategory category = ProductCategory.builder()
                .productCategoryId(1L)
                .productCategoryName("Clothing")
                .isActive(false) // Inactive category
                .build();

        when(productCategoryRepository.findById(1L)).thenReturn(Optional.of(category));
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> productCategoryService.getCategoryByCategoryId(1L));

    }

    @Test
    void testGetCategoryById_ThrowsProductCategoryException() {
  
        doThrow(new ProductCategoryException("Simulated category error")).when(productCategoryRepository).findById(1L);

        ProductCategoryException exception = assertThrows(ProductCategoryException.class,
                () -> productCategoryService.getCategoryByCategoryId(1L));

        assertTrue(exception.getMessage().contains("Simulated category error"));
        verify(productCategoryRepository).findById(1L);
    }

    // Test for generic RuntimeException in getCategoryByCategoryId
    @Test
    void testGetCategoryById_ThrowsRuntimeException() {
        // Simulate a generic RuntimeException from the repository
        when(productCategoryRepository.findById(1L)).thenThrow(new RuntimeException("Database connection issue"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> productCategoryService.getCategoryByCategoryId(1L));

        assertTrue(exception.getMessage().contains("Database connection issue"));
        verify(productCategoryRepository).findById(1L);
    }


    //--------------------------------------------test Update Category-----------------------------------------------------------------------------------
    @Test
    void testUpdateCategory_Success() {
        ProductCategory existing = ProductCategory.builder()
                .productCategoryId(1L)
                .productCategoryName("Old")
                .isActive(true)
                .build();

        ProductCategory updated = ProductCategory.builder()
                .productCategoryId(1L)
                .productCategoryName("Updated")
                .isActive(true)
                .build();

        when(productCategoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productCategoryRepository.save(any(ProductCategory.class))).thenReturn(updated);

        ProductCategoryDTO dto = new ProductCategoryDTO(1L, "Updated", true);
        ProductCategoryDTO result = productCategoryService.updateCategoryByCategoryId(1L, dto);

        assertEquals("Updated", result.getProductCategoryName());
        verify(productCategoryRepository).findById(1L);
        verify(productCategoryRepository).save(existing); // Verify save was called with the modified existing entity
    }

    // Negative: updateCategory - not found
    @Test
    void testUpdateCategory_NotFound() {
        ProductCategoryDTO dto = new ProductCategoryDTO(1L, "Updated", true);
        when(productCategoryRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> productCategoryService.updateCategoryByCategoryId(1L, dto));

    }

    @Test
    void testUpdateCategory_ThrowsProductCategoryException() {
        // Simulate a ProductCategoryException during update
        ProductCategoryDTO dto = new ProductCategoryDTO(1L, "Updated", true);
        ProductCategory existing = ProductCategory.builder()
                .productCategoryId(1L)
                .productCategoryName("Old")
                .isActive(true)
                .build();
        when(productCategoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        doThrow(new ProductCategoryException("Update failed")).when(productCategoryRepository).save(any(ProductCategory.class));

        ProductCategoryException exception = assertThrows(ProductCategoryException.class,
                () -> productCategoryService.updateCategoryByCategoryId(1L, dto));

        assertTrue(exception.getMessage().contains("Update failed"));
        verify(productCategoryRepository).findById(1L);
        verify(productCategoryRepository).save(any(ProductCategory.class));
    }

    // Test for generic RuntimeException in updateCategoryByCategoryId
    @Test
    void testUpdateCategory_ThrowsRuntimeException() {
        ProductCategoryDTO dto = new ProductCategoryDTO(1L, "Updated", true);
        ProductCategory existing = ProductCategory.builder()
                .productCategoryId(1L)
                .productCategoryName("Old")
                .isActive(true)
                .build();
        when(productCategoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        // Simulate a generic RuntimeException during save
        when(productCategoryRepository.save(any(ProductCategory.class))).thenThrow(new RuntimeException("Database write error"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> productCategoryService.updateCategoryByCategoryId(1L, dto));

        assertTrue(exception.getMessage().contains("Database write error"));
        verify(productCategoryRepository).findById(1L);
        verify(productCategoryRepository).save(any(ProductCategory.class));
    }


    //--------------------------------------------test Get All Categories-----------------------------------------------------------------------------------
    @Test
    void testGetAllCategories_Success() {
        List<ProductCategory> categories = List.of(
                ProductCategory.builder().productCategoryId(1L).productCategoryName("Mobiles").isActive(true).build(),
                ProductCategory.builder().productCategoryId(2L).productCategoryName("Laptops").isActive(false).build(),
                ProductCategory.builder().productCategoryId(3L).productCategoryName("TVs").isActive(true).build()
        );

        when(productCategoryRepository.findAll()).thenReturn(categories);

        List<ProductCategoryDTO> result = productCategoryService.getAllCategories();

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(ProductCategoryDTO::getIsActive));
        // Verify that only active categories are returned and mapped correctly
        assertTrue(result.stream().anyMatch(dto -> dto.getProductCategoryName().equals("Mobiles")));
        assertTrue(result.stream().anyMatch(dto -> dto.getProductCategoryName().equals("TVs")));
        assertFalse(result.stream().anyMatch(dto -> dto.getProductCategoryName().equals("Laptops")));
        verify(productCategoryRepository).findAll();
    }


    @Test
    void testGetAllCategories_ThrowsProductCategoryException() {
        // Simulate a ProductCategoryException during fetch
        doThrow(new ProductCategoryException("Category fetch error")).when(productCategoryRepository).findAll();

        ProductCategoryException exception = assertThrows(ProductCategoryException.class,
                () -> productCategoryService.getAllCategories());

        assertTrue(exception.getMessage().contains("Category fetch error"));
        verify(productCategoryRepository).findAll();
    }

    // Test for generic RuntimeException in getAllCategories
    @Test
    void testGetAllCategories_ThrowsRuntimeException() {
        // Simulate a generic RuntimeException from the repository
        when(productCategoryRepository.findAll()).thenThrow(new RuntimeException("Network error during data retrieval"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> productCategoryService.getAllCategories());

        assertTrue(exception.getMessage().contains("Network error during data retrieval"));
        verify(productCategoryRepository).findAll();
    }


    //--------------------------------------------test Delete Category-----------------------------------------------------------------------------------
    @Test
    void testDeleteCategory_Success() {
        ProductCategory category = ProductCategory.builder()
                .productCategoryId(1L)
                .productCategoryName("Books")
                .isActive(true)
                .build();

        when(productCategoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productCategoryRepository.save(any(ProductCategory.class))).thenReturn(category); // Soft delete will save

        productCategoryService.deleteCategoryByCategoryId(1L);

        assertFalse(category.getIsActive()); // Verify isActive is set to false
        verify(productCategoryRepository).findById(1L);
        verify(productCategoryRepository).save(category); // Verify save was called for soft delete
    }



    @Test
    void testDeleteCategory_ThrowsProductCategoryException() {
        // Simulate a ProductCategoryException during deletion process
        doThrow(new ProductCategoryException("Deletion constraint violated")).when(productCategoryRepository).findById(1L);

        ProductCategoryException exception = assertThrows(ProductCategoryException.class,
                () -> productCategoryService.deleteCategoryByCategoryId(1L));

        assertTrue(exception.getMessage().contains("Deletion constraint violated"));
        verify(productCategoryRepository).findById(1L);
        verify(productCategoryRepository, never()).save(any(ProductCategory.class));
    }

    // Test for generic RuntimeException in deleteCategoryByCategoryId
    @Test
    void testDeleteCategory_ThrowsRuntimeException() {
        ProductCategory category = ProductCategory.builder()
                .productCategoryId(1L)
                .productCategoryName("Books")
                .isActive(true)
                .build();
        when(productCategoryRepository.findById(1L)).thenReturn(Optional.of(category));
        // Simulate a generic RuntimeException during save (soft delete)
        when(productCategoryRepository.save(any(ProductCategory.class))).thenThrow(new RuntimeException("Database access denied"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> productCategoryService.deleteCategoryByCategoryId(1L));

        assertTrue(exception.getMessage().contains("Database access denied"));
        verify(productCategoryRepository).findById(1L);
        verify(productCategoryRepository).save(any(ProductCategory.class));
    }
}