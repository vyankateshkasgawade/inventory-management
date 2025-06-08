package com.application.controller;

import com.application.dto.ProductCategoryDTO;
import com.application.exception.ProductCategoryException;
import com.application.exception.ResourceNotFoundException;
import com.application.service.ProductCategoryService;
import com.application.util.AppConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductCategoryControllerTest {

    @Mock
    private ProductCategoryService productCategoryService;

    @InjectMocks
    private ProductCategoryController productCategoryController;

    private ProductCategoryDTO validCategoryDTO;
    private ProductCategoryDTO existingCategoryDTO;

    @BeforeEach
    void setUp() {
        validCategoryDTO = ProductCategoryDTO.builder()
                .productCategoryId(1L)
                .productCategoryName("Electronics")
                .isActive(true)
                .build();

        existingCategoryDTO = ProductCategoryDTO.builder()
                .productCategoryId(2L)
                .productCategoryName("Clothing")
                .isActive(true)
                .build();
    }
//----------------------------------- create New Category Test --------------------------------------------------------------------
    @Test
    void createNewCategory_ShouldReturnCreated_WhenValidInput() {
        when(productCategoryService.createCategory(any(ProductCategoryDTO.class)))
            .thenReturn(validCategoryDTO);

        ResponseEntity<?> response = productCategoryController.createNewCategory(validCategoryDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(validCategoryDTO, response.getBody());
        verify(productCategoryService, times(1)).createCategory(any(ProductCategoryDTO.class));
    }

    @Test
    void createNewCategory_ShouldReturnBadRequest_WhenInvalidInput() {
        ProductCategoryDTO invalidDTO = new ProductCategoryDTO();
        invalidDTO.setProductCategoryName("");

        when(productCategoryService.createCategory(any(ProductCategoryDTO.class)))
            .thenThrow(new IllegalArgumentException("Category name cannot be empty"));

        ResponseEntity<?> response = productCategoryController.createNewCategory(invalidDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Invalid input"));
        verify(productCategoryService).createCategory(any(ProductCategoryDTO.class));
    }

    @Test
    void createNewCategory_ShouldReturnConflict_WhenCategoryExists() {
        when(productCategoryService.createCategory(any(ProductCategoryDTO.class)))
            .thenThrow(new ProductCategoryException("Category already exists"));

        ResponseEntity<?> response = productCategoryController.createNewCategory(validCategoryDTO);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Category already exists", response.getBody());
        verify(productCategoryService).createCategory(any(ProductCategoryDTO.class));
    }

    @Test
    void createNewCategory_ShouldReturnConflict_WhenDataIntegrityViolation() {
        when(productCategoryService.createCategory(any(ProductCategoryDTO.class)))
            .thenThrow(new DataIntegrityViolationException("Constraint violation"));

        ResponseEntity<?> response = productCategoryController.createNewCategory(validCategoryDTO);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Conflict: DB constraint violation", response.getBody());
        verify(productCategoryService).createCategory(any(ProductCategoryDTO.class));
    }

    @Test
    void createNewCategory_ShouldReturnInternalServerError_WhenUnexpectedError() {
        when(productCategoryService.createCategory(any(ProductCategoryDTO.class)))
            .thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<?> response = productCategoryController.createNewCategory(validCategoryDTO);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(AppConstants.SOMETHING_WENT_WRONG, response.getBody());
        verify(productCategoryService).createCategory(any(ProductCategoryDTO.class));
    }

    
    
  //--------------------------------------get Category By Category Id-------------------------------------------------------------------
    @Test
    void getCategoryByCategoryId_ShouldReturnOk_WhenCategoryExists() {
        when(productCategoryService.getCategoryByCategoryId(anyLong())).thenReturn(validCategoryDTO);

        ResponseEntity<?> response = productCategoryController.getCategoryByCategoryId(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(validCategoryDTO, response.getBody());
        verify(productCategoryService).getCategoryByCategoryId(1L);
    }

    @Test
    void getCategoryByCategoryId_ShouldReturnNotFound_WhenCategoryNotFound() {
        when(productCategoryService.getCategoryByCategoryId(anyLong()))
                .thenThrow(new ResourceNotFoundException(AppConstants.CATEGORY_NOT_FOUND + 1L));

        ResponseEntity<?> response = productCategoryController.getCategoryByCategoryId(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().toString().contains(AppConstants.CATEGORY_NOT_FOUND));
        verify(productCategoryService).getCategoryByCategoryId(1L);
    }

    @Test
    void getCategoryByCategoryId_ShouldReturnBadRequest_WhenInvalidId() {
        when(productCategoryService.getCategoryByCategoryId(anyLong()))
                .thenThrow(new IllegalArgumentException("Invalid category ID"));

        ResponseEntity<?> response = productCategoryController.getCategoryByCategoryId(-1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Invalid category ID"));
        verify(productCategoryService).getCategoryByCategoryId(-1L);
    }

    @Test
    void getCategoryByCategoryId_ShouldReturnInternalServerError_WhenProductCategoryExceptionThrown() {
        when(productCategoryService.getCategoryByCategoryId(anyLong()))
                .thenThrow(new ProductCategoryException("Service failure"));

        ResponseEntity<?> response = productCategoryController.getCategoryByCategoryId(1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Service failure", response.getBody());
        verify(productCategoryService).getCategoryByCategoryId(1L);
    }

    @Test
    void getCategoryByCategoryId_ShouldReturnInternalServerError_WhenUnexpectedError() {
        when(productCategoryService.getCategoryByCategoryId(anyLong()))
                .thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<?> response = productCategoryController.getCategoryByCategoryId(1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(AppConstants.SOMETHING_WENT_WRONG, response.getBody());
        verify(productCategoryService).getCategoryByCategoryId(1L);
    }

    
    //----------------------------------get All Product Categories Test --------------------------------------------------------------------------
    @Test
    void getAllProductCategories_ShouldReturnOk_WhenCategoriesExist() {
        List<ProductCategoryDTO> categories = Arrays.asList(validCategoryDTO, existingCategoryDTO);
        when(productCategoryService.getAllCategories()).thenReturn(categories);

        ResponseEntity<?> response = productCategoryController.getAllProductCategories();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(categories, response.getBody());
        verify(productCategoryService).getAllCategories();
    }

    @Test
    void getAllProductCategories_ShouldReturnOk_WhenNoCategoriesExist() {
        when(productCategoryService.getAllCategories()).thenReturn(List.of());

        ResponseEntity<?> response = productCategoryController.getAllProductCategories();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(((List<?>) response.getBody()).isEmpty());
        verify(productCategoryService).getAllCategories();
    }

    @Test
    void getAllProductCategories_ShouldReturnInternalServerError_WhenProductCategoryExceptionThrown() {
        when(productCategoryService.getAllCategories())
                .thenThrow(new ProductCategoryException("Service failed"));

        ResponseEntity<?> response = productCategoryController.getAllProductCategories();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Service failed", response.getBody());
        verify(productCategoryService).getAllCategories();
    }

    @Test
    void getAllProductCategories_ShouldReturnInternalServerError_WhenUnexpectedError() {
        when(productCategoryService.getAllCategories())
                .thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<?> response = productCategoryController.getAllProductCategories();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(AppConstants.SOMETHING_WENT_WRONG, response.getBody());
        verify(productCategoryService).getAllCategories();
    }

//-----------------------------------------update Category By Category Id-------------------------------------------------------------------------------------
    @Test
    void updateCategoryByCategoryId_ShouldReturnOk_WhenValidInput() {
        when(productCategoryService.updateCategoryByCategoryId(anyLong(), any(ProductCategoryDTO.class)))
                .thenReturn(validCategoryDTO);

        ResponseEntity<?> response = productCategoryController.updateCategoryByCategoryId(1L, validCategoryDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(validCategoryDTO, response.getBody());
        verify(productCategoryService).updateCategoryByCategoryId(anyLong(), any(ProductCategoryDTO.class));
    }

    @Test
    void updateCategoryByCategoryId_ShouldReturnNotFound_WhenCategoryNotFound() {
        when(productCategoryService.updateCategoryByCategoryId(anyLong(), any(ProductCategoryDTO.class)))
                .thenThrow(new ResourceNotFoundException(AppConstants.CATEGORY_NOT_FOUND + 1L));

        ResponseEntity<?> response = productCategoryController.updateCategoryByCategoryId(1L, validCategoryDTO);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().toString().contains(AppConstants.CATEGORY_NOT_FOUND));
        verify(productCategoryService).updateCategoryByCategoryId(anyLong(), any(ProductCategoryDTO.class));
    }

    @Test
    void updateCategoryByCategoryId_ShouldReturnBadRequest_WhenInvalidInput() {
        ProductCategoryDTO invalidDTO = new ProductCategoryDTO();
        invalidDTO.setProductCategoryName("");

        when(productCategoryService.updateCategoryByCategoryId(anyLong(), any(ProductCategoryDTO.class)))
                .thenThrow(new IllegalArgumentException("Invalid category name"));

        ResponseEntity<?> response = productCategoryController.updateCategoryByCategoryId(1L, invalidDTO);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Invalid input"));
        verify(productCategoryService).updateCategoryByCategoryId(anyLong(), any(ProductCategoryDTO.class));
    }

    @Test
    void updateCategoryByCategoryId_ShouldReturnInternalServerError_WhenProductCategoryExceptionThrown() {
        when(productCategoryService.updateCategoryByCategoryId(anyLong(), any(ProductCategoryDTO.class)))
                .thenThrow(new ProductCategoryException("Service failure"));

        ResponseEntity<?> response = productCategoryController.updateCategoryByCategoryId(1L, validCategoryDTO);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Service failure", response.getBody());
        verify(productCategoryService).updateCategoryByCategoryId(anyLong(), any(ProductCategoryDTO.class));
    }

    @Test
    void updateCategoryByCategoryId_ShouldReturnInternalServerError_WhenUnexpectedError() {
        when(productCategoryService.updateCategoryByCategoryId(anyLong(), any(ProductCategoryDTO.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<?> response = productCategoryController.updateCategoryByCategoryId(1L, validCategoryDTO);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(AppConstants.SOMETHING_WENT_WRONG, response.getBody());
        verify(productCategoryService).updateCategoryByCategoryId(anyLong(), any(ProductCategoryDTO.class));
    }

    
    
    
    //------------------------------------------Delete Product Test -----------------------------------------------------------------------------------
    @Test
    void deleteCategoryByCategoryId_ShouldReturnNoContent_WhenSuccessful() {
        doNothing().when(productCategoryService).deleteCategoryByCategoryId(anyLong());

        ResponseEntity<?> response = productCategoryController.deleteCategoryByCategoryId(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(productCategoryService).deleteCategoryByCategoryId(anyLong());
    }

    @Test
    void deleteCategoryByCategoryId_ShouldReturnNotFound_WhenCategoryNotFound() {
        doThrow(new ResourceNotFoundException(AppConstants.CATEGORY_NOT_FOUND + 1L))
                .when(productCategoryService).deleteCategoryByCategoryId(anyLong());

        ResponseEntity<?> response = productCategoryController.deleteCategoryByCategoryId(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().toString().contains(AppConstants.CATEGORY_NOT_FOUND));
        verify(productCategoryService).deleteCategoryByCategoryId(anyLong());
    }

    @Test
    void deleteCategoryByCategoryId_ShouldReturnBadRequest_WhenInvalidId() {
        doThrow(new IllegalArgumentException("Invalid category ID"))
                .when(productCategoryService).deleteCategoryByCategoryId(anyLong());

        ResponseEntity<?> response = productCategoryController.deleteCategoryByCategoryId(-1L);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().toString().contains("Invalid category ID"));
        verify(productCategoryService).deleteCategoryByCategoryId(anyLong());
    }

    @Test
    void deleteCategoryByCategoryId_ShouldReturnInternalServerError_WhenUnexpectedError() {
        doThrow(new RuntimeException("Unexpected error"))
                .when(productCategoryService).deleteCategoryByCategoryId(anyLong());

        ResponseEntity<?> response = productCategoryController.deleteCategoryByCategoryId(1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(AppConstants.SOMETHING_WENT_WRONG, response.getBody());
        verify(productCategoryService).deleteCategoryByCategoryId(anyLong());
    }

}