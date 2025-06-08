package com.application.service.impl;

import com.application.dto.ProductDetailsDTO;
import com.application.dto.ProductOrderUpdateDTO;
import com.application.entity.ProductCategory;
import com.application.entity.ProductDetails;
import com.application.exception.ResourceNotFoundException;
import com.application.repository.ProductCategoryRepository;
import com.application.repository.ProductDetailsRepository;
import com.application.util.AppConstants;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Arrays;
import java.util.HashSet; // Added for updatedProductIds set in mocks
import java.util.Set;    // Added for updatedProductIds set in mocks


import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductDetailsServiceImplTest {

    @InjectMocks
    private ProductDetailsServiceImpl productDetailsService;

    @Mock
    private ProductDetailsRepository productRepository;

    @Mock
    private ProductCategoryRepository categoryRepository;

    //----------------------------------------------test Create Product Success------------------------------------------------------------------
    @Test
    void testCreateProduct_Success() {
        ProductDetailsDTO dto = ProductDetailsDTO.builder()
                .productName("Phone")
                .productQuantity(10)
                .price(new BigDecimal("20000.0"))
                .productCategoryId(1L)
                .imageUrl("image.jpg")
                .isActive(true)
                .trendingDisplayOrder(null)
                .sellingDisplayOrder(null)
                .build();
        ProductCategory category = ProductCategory.builder().productCategoryId(1L).productCategoryName("Electronics").isActive(true).build();

        when(productRepository.findByProductNameAndIsActiveTrue("Phone")).thenReturn(Optional.empty());
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        ProductDetails saved = ProductDetails.builder()
                .productDetailsId(1L)
                .productName("Phone")
                .productQuantity(10)
                .price(new BigDecimal("20000.0"))
                .productCategory(category)
                .isActive(true)
                .imageUrl("image.jpg")
                .trendingDisplayOrder(null)
                .sellingDisplayOrder(null)
                .build();

        when(productRepository.save(any(ProductDetails.class))).thenReturn(saved);

        ProductDetailsDTO result = productDetailsService.createProduct(dto);

        assertEquals("Phone", result.getProductName());
        assertEquals(new BigDecimal("20000.0"), result.getPrice());
        assertEquals("Electronics", result.getProductCategoryName());
        assertTrue(result.getIsActive());
        assertNull(result.getTrendingDisplayOrder());
        assertNull(result.getSellingDisplayOrder());
        verify(productRepository).findByProductNameAndIsActiveTrue("Phone");
        verify(categoryRepository).findById(1L);
        verify(productRepository).save(any(ProductDetails.class));
    }


    @Test
    void testCreateProduct_ShouldThrowRuntimeException_WhenProductNameExists() {
        ProductDetailsDTO dto = ProductDetailsDTO.builder()
                .productName("Phone")
                .productQuantity(10)
                .price(new BigDecimal("20000.0"))
                .productCategoryId(1L)
                .isActive(true)
                .build();
        when(productRepository.findByProductNameAndIsActiveTrue("Phone"))
                .thenReturn(Optional.of(new ProductDetails()));

        assertThatThrownBy(() -> productDetailsService.createProduct(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Product with name 'Phone' already exists.");
        verify(productRepository).findByProductNameAndIsActiveTrue("Phone");
        verifyNoInteractions(categoryRepository);
        verifyNoMoreInteractions(productRepository); // No save should be called
    }

    @Test
    void testCreateProduct_ShouldThrowResourceNotFoundException_WhenCategoryNotFound() {
        ProductDetailsDTO dto = ProductDetailsDTO.builder()
                .productName("Phone")
                .productQuantity(10)
                .price(new BigDecimal("20000.0"))
                .productCategoryId(999L)
                .isActive(true)
                .build();
        when(productRepository.findByProductNameAndIsActiveTrue("Phone")).thenReturn(Optional.empty());
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productDetailsService.createProduct(dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(AppConstants.CATEGORY_NOT_FOUND + 999L);
        verify(productRepository).findByProductNameAndIsActiveTrue("Phone");
        verify(categoryRepository).findById(999L);
        verifyNoMoreInteractions(productRepository); // No save should be called
    }

    @Test
    void testCreateProduct_ShouldThrowRuntimeException_WhenConstraintViolationOccurs() {
        ProductDetailsDTO dto = ProductDetailsDTO.builder()
                .productName("Phone")
                .productQuantity(10)
                .price(new BigDecimal("20000.0"))
                .productCategoryId(1L)
                .isActive(true)
                .build();
        ProductCategory category = ProductCategory.builder().productCategoryId(1L).build();

        when(productRepository.findByProductNameAndIsActiveTrue("Phone")).thenReturn(Optional.empty());
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(ProductDetails.class)))
                .thenThrow(new DataIntegrityViolationException("unique constraint [product_name]"));

        assertThatThrownBy(() -> productDetailsService.createProduct(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Product with name 'Phone' already exists.");
        verify(productRepository).findByProductNameAndIsActiveTrue("Phone");
        verify(categoryRepository).findById(1L);
        verify(productRepository).save(any(ProductDetails.class));
    }

    @Test
    void testCreateProduct_ShouldThrowRuntimeException_WhenOtherDataIntegrityViolationOccurs() {
        ProductDetailsDTO dto = ProductDetailsDTO.builder()
                .productName("Phone")
                .productQuantity(10)
                .price(new BigDecimal("20000.0"))
                .productCategoryId(1L)
                .isActive(true)
                .build();
        ProductCategory category = ProductCategory.builder().productCategoryId(1L).build();

        when(productRepository.findByProductNameAndIsActiveTrue("Phone")).thenReturn(Optional.empty());
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(ProductDetails.class)))
                .thenThrow(new DataIntegrityViolationException("some other db error"));

        assertThatThrownBy(() -> productDetailsService.createProduct(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error saving product: some other db error");
        verify(productRepository).findByProductNameAndIsActiveTrue("Phone");
        verify(categoryRepository).findById(1L);
        verify(productRepository).save(any(ProductDetails.class));
    }

    @Test
    void testCreateProduct_ShouldThrowRuntimeException_WhenUnexpectedRuntimeExceptionOccurs() {
        ProductDetailsDTO dto = ProductDetailsDTO.builder()
                .productName("Phone")
                .productQuantity(10)
                .price(new BigDecimal("20000.0"))
                .productCategoryId(1L)
                .isActive(true)
                .build();
        // Simulate an unexpected runtime exception from the very first repository call
        when(productRepository.findByProductNameAndIsActiveTrue("Phone")).thenThrow(new IllegalStateException("Unexpected error during name check"));

        assertThatThrownBy(() -> productDetailsService.createProduct(dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unexpected error during name check");
        verify(productRepository).findByProductNameAndIsActiveTrue("Phone");
        verifyNoInteractions(categoryRepository);
        verifyNoMoreInteractions(productRepository);
    }

    //-------------------------------------------------test Get Product By Id --------------------------------------------------------------------------
    @Test
    void testGetProductById_Success() {
        ProductCategory category = ProductCategory.builder()
                .productCategoryId(1L)
                .productCategoryName("Electronics")
                .isActive(true)
                .build();

        ProductDetails product = ProductDetails.builder()
                .productDetailsId(1L)
                .productName("Phone")
                .productQuantity(5)
                .price(new BigDecimal("10000.0"))
                .isActive(true)
                .productCategory(category)
                .imageUrl("phone.jpg")
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductDetailsDTO result = productDetailsService.getProductByProductId(1L);
        assertEquals("Phone", result.getProductName());
        assertEquals(new BigDecimal("10000.0"), result.getPrice());
        assertEquals("Electronics", result.getProductCategoryName());
        assertEquals("phone.jpg", result.getImageUrl());
        assertTrue(result.getIsActive());
        verify(productRepository).findById(1L);
    }

    @Test
    void testGetProductById_NotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productDetailsService.getProductByProductId(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(AppConstants.PRODUCT_NOT_FOUND + 1L);
        verify(productRepository).findById(1L);
    }

    @Test
    void testGetProductById_Inactive() {
        ProductDetails product = ProductDetails.builder()
                .productDetailsId(1L)
                .productName("Phone")
                .productQuantity(5)
                .price(new BigDecimal("10000.0"))
                .isActive(false) // Product is inactive
                .productCategory(ProductCategory.builder().productCategoryId(1L).build())
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productDetailsService.getProductByProductId(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(AppConstants.PRODUCT_NOT_FOUND + 1L);
        verify(productRepository).findById(1L);
    }

    @Test
    void testGetProductById_ShouldThrowRuntimeException_WhenUnexpectedRuntimeOccurs() {
        when(productRepository.findById(anyLong())).thenThrow(new IllegalStateException("DB connection failure"));

        assertThatThrownBy(() -> productDetailsService.getProductByProductId(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB connection failure");
        verify(productRepository).findById(1L);
    }

    //---------------------------------------------------test Get All Products -------------------------------------------------------------------------
    @Test
    void testGetAllProducts_Success() {
        ProductCategory category = ProductCategory.builder()
                .productCategoryId(1L)
                .productCategoryName("Electronics")
                .isActive(true)
                .build();

        List<ProductDetails> products = List.of(
                ProductDetails.builder().productDetailsId(1L).productName("Phone").productQuantity(5).price(new BigDecimal("10000.0")).isActive(true).productCategory(category).imageUrl("phone.jpg").build(),
                ProductDetails.builder().productDetailsId(2L).productName("Laptop").productQuantity(3).price(new BigDecimal("50000.0")).isActive(false).productCategory(category).imageUrl("laptop.jpg").build(),
                ProductDetails.builder().productDetailsId(3L).productName("Tablet").productQuantity(2).price(new BigDecimal("25000.0")).isActive(true).productCategory(category).imageUrl("tablet.jpg").build()
        );

        when(productRepository.findAll()).thenReturn(products);

        List<ProductDetailsDTO> result = productDetailsService.getAllProducts();

        assertEquals(3, result.size()); // All products are mapped, isActive filter is not applied here
        assertTrue(result.stream().anyMatch(dto -> dto.getProductName().equals("Phone")));
        assertTrue(result.stream().anyMatch(dto -> dto.getProductName().equals("Laptop")));
        assertTrue(result.stream().anyMatch(dto -> dto.getProductName().equals("Tablet")));
        verify(productRepository).findAll();
    }

    @Test
    void testGetAllProducts_ShouldReturnEmptyList_WhenNoProductsFound() {
        when(productRepository.findAll()).thenReturn(Collections.emptyList());

        List<ProductDetailsDTO> result = productDetailsService.getAllProducts();
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository).findAll();
    }

    @Test
    void testGetAllProducts_ShouldThrowRuntimeException_WhenUnexpectedRuntimeOccurs() {
        when(productRepository.findAll()).thenThrow(new IllegalStateException("Database fetch error"));

        assertThatThrownBy(() -> productDetailsService.getAllProducts())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database fetch error");
        verify(productRepository).findAll();
    }

    //---------------------------------------------------test Update Product -----------------------------------------------------------------------------
    @Test
    void testUpdateProduct_Success() {
        ProductCategory oldCategory = ProductCategory.builder().productCategoryId(1L).productCategoryName("Old Category").build();
        ProductCategory newCategory = ProductCategory.builder().productCategoryId(2L).productCategoryName("New Category").build();

        ProductDetails existing = ProductDetails.builder()
                .productDetailsId(1L)
                .productName("Old Product")
                .productQuantity(5)
                .price(new BigDecimal("10000.0"))
                .isActive(true)
                .productCategory(oldCategory)
                .imageUrl("old.jpg")
                .trendingDisplayOrder(1) // Should remain unchanged
                .sellingDisplayOrder(2)  // Should remain unchanged
                .build();

        ProductDetailsDTO dto = ProductDetailsDTO.builder()
                .productDetailsId(1L) // Include ID for the DTO for update scenario
                .productName("Updated Product")
                .productQuantity(10)
                .price(new BigDecimal("15000.0"))
                .productCategoryId(2L)
                .imageUrl("new.jpg")
                .isActive(false)
                .trendingDisplayOrder(null) // These are not updated by this method
                .sellingDisplayOrder(null)  // These are not updated by this method
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.findByProductNameAndIsActiveTrue("Updated Product")).thenReturn(Optional.empty()); // No other active product with this name
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(newCategory));

        // The returned entity should reflect the updates, but preserve trending/selling orders
        ProductDetails updatedEntity = ProductDetails.builder()
                .productDetailsId(1L)
                .productName("Updated Product")
                .productQuantity(10)
                .price(new BigDecimal("15000.0"))
                .productCategory(newCategory)
                .isActive(false)
                .imageUrl("new.jpg")
                .trendingDisplayOrder(1) // Still 1 from existing
                .sellingDisplayOrder(2) // Still 2 from existing
                .build();

        when(productRepository.save(any(ProductDetails.class))).thenReturn(updatedEntity);

        ProductDetailsDTO result = productDetailsService.updateProductByProductId(1L, dto);

        assertEquals("Updated Product", result.getProductName());
        assertEquals(10, result.getProductQuantity());
        assertEquals(new BigDecimal("15000.0"), result.getPrice());
        assertEquals(2L, result.getProductCategoryId());
        assertEquals("New Category", result.getProductCategoryName());
        assertFalse(result.getIsActive());
        assertEquals("new.jpg", result.getImageUrl());
        assertEquals(1, result.getTrendingDisplayOrder()); // Ensure original value is retained
        assertEquals(2, result.getSellingDisplayOrder());  // Ensure original value is retained

        verify(productRepository).findById(1L);
        verify(productRepository).findByProductNameAndIsActiveTrue("Updated Product");
        verify(categoryRepository).findById(2L);
        verify(productRepository).save(existing); // Verify save was called with the modified existing entity
    }

    @Test
    void testUpdateProduct_NotFound() {
        ProductDetailsDTO dto = ProductDetailsDTO.builder()
                .productDetailsId(1L)
                .productName("New")
                .productQuantity(10)
                .price(new BigDecimal("15000.0"))
                .productCategoryId(1L)
                .isActive(true)
                .build();
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productDetailsService.updateProductByProductId(1L, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(AppConstants.PRODUCT_NOT_FOUND + 1L);
        verify(productRepository).findById(1L);
        verifyNoMoreInteractions(productRepository);
        verifyNoInteractions(categoryRepository);
    }

    @Test
    void testUpdateProduct_DuplicateName() {
        ProductDetails existing = ProductDetails.builder()
                .productDetailsId(1L)
                .productName("Old Product")
                .productQuantity(5)
                .price(new BigDecimal("10000.0"))
                .isActive(true)
                .productCategory(ProductCategory.builder().productCategoryId(1L).build())
                .build();

        ProductDetails otherActiveProductWithSameName = ProductDetails.builder()
                .productDetailsId(2L)
                .productName("Existing Product Name")
                .productQuantity(10)
                .price(new BigDecimal("15000.0"))
                .isActive(true)
                .productCategory(ProductCategory.builder().productCategoryId(1L).build())
                .build();

        ProductDetailsDTO dto = ProductDetailsDTO.builder()
                .productDetailsId(1L)
                .productName("Existing Product Name")
                .productQuantity(10)
                .price(new BigDecimal("15000.0"))
                .productCategoryId(1L)
                .isActive(true)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.findByProductNameAndIsActiveTrue("Existing Product Name")).thenReturn(Optional.of(otherActiveProductWithSameName));

        assertThatThrownBy(() -> productDetailsService.updateProductByProductId(1L, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Product with name 'Existing Product Name' already exists.");
        verify(productRepository).findById(1L);
        verify(productRepository).findByProductNameAndIsActiveTrue("Existing Product Name");
        verifyNoInteractions(categoryRepository);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    void testUpdateProduct_CategoryNotFound() {
        ProductDetails existing = ProductDetails.builder()
                .productDetailsId(1L)
                .productName("Old Product")
                .productQuantity(5)
                .price(new BigDecimal("10000.0"))
                .isActive(true)
                .productCategory(ProductCategory.builder().productCategoryId(1L).build())
                .build();

        ProductDetailsDTO dto = ProductDetailsDTO.builder()
                .productDetailsId(1L)
                .productName("Updated Product")
                .productQuantity(10)
                .price(new BigDecimal("15000.0"))
                .productCategoryId(99L)
                .isActive(true)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.findByProductNameAndIsActiveTrue("Updated Product")).thenReturn(Optional.empty());
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productDetailsService.updateProductByProductId(1L, dto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(AppConstants.CATEGORY_NOT_FOUND + 99L);
        verify(productRepository).findById(1L);
        verify(productRepository).findByProductNameAndIsActiveTrue("Updated Product");
        verify(categoryRepository).findById(99L);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    void testUpdateProduct_ShouldThrowRuntimeException_WhenUnexpectedRuntimeOccurs() {
        ProductDetails existing = ProductDetails.builder()
                .productDetailsId(1L)
                .productName("Old Product")
                .productQuantity(5)
                .price(new BigDecimal("10000.0"))
                .isActive(true)
                .productCategory(ProductCategory.builder().productCategoryId(1L).build())
                .build();

        ProductDetailsDTO dto = ProductDetailsDTO.builder()
                .productDetailsId(1L)
                .productName("New Product")
                .productQuantity(10)
                .price(new BigDecimal("15000.0"))
                .productCategoryId(1L)
                .isActive(true)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.findByProductNameAndIsActiveTrue("New Product")).thenReturn(Optional.empty());
        when(categoryRepository.findById(1L)).thenThrow(new IllegalStateException("Category service unavailable")); // Simulate runtime error

        assertThatThrownBy(() -> productDetailsService.updateProductByProductId(1L, dto))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Category service unavailable");
        verify(productRepository).findById(1L);
        verify(productRepository).findByProductNameAndIsActiveTrue("New Product");
        verify(categoryRepository).findById(1L);
        verifyNoMoreInteractions(productRepository);
    }

    //---------------------------------------------------test Delete Product -----------------------------------------------------------------------------
    @Test
    void testDeleteProduct_Success() {
        ProductDetails product = ProductDetails.builder()
                .productDetailsId(1L)
                .productName("Test Product")
                .isActive(true)
                .trendingDisplayOrder(10) // Should be nullified on soft delete
                .sellingDisplayOrder(20)  // Should be nullified on soft delete
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(ProductDetails.class))).thenReturn(product);

        productDetailsService.deleteProductByProductId(1L);

        assertFalse(product.getIsActive()); // Verify soft delete
        assertNull(product.getTrendingDisplayOrder()); // Verify nullification
        assertNull(product.getSellingDisplayOrder()); // Verify nullification
        verify(productRepository).findById(1L);
        verify(productRepository).save(product);
    }

    @Test
    void testDeleteProduct_NotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productDetailsService.deleteProductByProductId(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(AppConstants.PRODUCT_NOT_FOUND + 1L);
        verify(productRepository).findById(1L);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    void testDeleteProduct_ShouldThrowRuntimeException_WhenUnexpectedRuntimeOccurs() {
        ProductDetails product = ProductDetails.builder()
                .productDetailsId(1L)
                .productName("Test Product")
                .isActive(true)
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(ProductDetails.class))).thenThrow(new RuntimeException("Database write error during soft delete"));

        assertThatThrownBy(() -> productDetailsService.deleteProductByProductId(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database write error during soft delete");
        verify(productRepository).findById(1L);
        verify(productRepository).save(any(ProductDetails.class));
    }


    //---------------------------------------------------test Get Top Trending Products -----------------------------------------------------------------------------
    @Test
    void testGetTopTrendingProducts_Success() {
        ProductCategory category = ProductCategory.builder().productCategoryId(1L).productCategoryName("Electronics").build();

        List<ProductDetails> trendingProducts = List.of(
                ProductDetails.builder().productDetailsId(1L).productName("Trend 1").trendingDisplayOrder(1).isActive(true).productCategory(category).build(),
                ProductDetails.builder().productDetailsId(2L).productName("Trend 2").trendingDisplayOrder(2).isActive(true).productCategory(category).build()
        );

        when(productRepository.findByTrendingDisplayOrderIsNotNullAndIsActiveTrueOrderByTrendingDisplayOrderAsc()).thenReturn(trendingProducts);

        List<ProductDetailsDTO> result = productDetailsService.getTopTrendingProducts();

        assertEquals(2, result.size());
        assertEquals("Trend 1", result.get(0).getProductName());
        assertEquals("Trend 2", result.get(1).getProductName());
        verify(productRepository).findByTrendingDisplayOrderIsNotNullAndIsActiveTrueOrderByTrendingDisplayOrderAsc();
    }

    @Test
    void testGetTopTrendingProducts_NoTrendingProducts() {
        when(productRepository.findByTrendingDisplayOrderIsNotNullAndIsActiveTrueOrderByTrendingDisplayOrderAsc()).thenReturn(Collections.emptyList());

        List<ProductDetailsDTO> result = productDetailsService.getTopTrendingProducts();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository).findByTrendingDisplayOrderIsNotNullAndIsActiveTrueOrderByTrendingDisplayOrderAsc();
    }

    @Test
    void testGetTopTrendingProducts_ShouldThrowRuntimeException_WhenUnexpectedRuntimeOccurs() {
        when(productRepository.findByTrendingDisplayOrderIsNotNullAndIsActiveTrueOrderByTrendingDisplayOrderAsc())
                .thenThrow(new RuntimeException("Failed to fetch trending data"));

        assertThatThrownBy(() -> productDetailsService.getTopTrendingProducts())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to fetch trending data");
        verify(productRepository).findByTrendingDisplayOrderIsNotNullAndIsActiveTrueOrderByTrendingDisplayOrderAsc();
    }

    //---------------------------------------------------test Get Top Selling Products -----------------------------------------------------------------------------
    @Test
    void testGetTopSellingProducts_Success() {
        ProductCategory category = ProductCategory.builder().productCategoryId(1L).productCategoryName("Electronics").build();

        List<ProductDetails> sellingProducts = List.of(
                ProductDetails.builder().productDetailsId(3L).productName("Sell 1").sellingDisplayOrder(1).isActive(true).productCategory(category).build(),
                ProductDetails.builder().productDetailsId(4L).productName("Sell 2").sellingDisplayOrder(2).isActive(true).productCategory(category).build()
        );

        when(productRepository.findBySellingDisplayOrderIsNotNullAndIsActiveTrueOrderBySellingDisplayOrderAsc()).thenReturn(sellingProducts);

        List<ProductDetailsDTO> result = productDetailsService.getTopSellingProducts();

        assertEquals(2, result.size());
        assertEquals("Sell 1", result.get(0).getProductName());
        assertEquals("Sell 2", result.get(1).getProductName());
        verify(productRepository).findBySellingDisplayOrderIsNotNullAndIsActiveTrueOrderBySellingDisplayOrderAsc();
    }

    @Test
    void testGetTopSellingProducts_NoSellingProducts() {
        when(productRepository.findBySellingDisplayOrderIsNotNullAndIsActiveTrueOrderBySellingDisplayOrderAsc()).thenReturn(Collections.emptyList());

        List<ProductDetailsDTO> result = productDetailsService.getTopSellingProducts();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository).findBySellingDisplayOrderIsNotNullAndIsActiveTrueOrderBySellingDisplayOrderAsc();
    }

    @Test
    void testGetTopSellingProducts_ShouldThrowRuntimeException_WhenUnexpectedRuntimeOccurs() {
        when(productRepository.findBySellingDisplayOrderIsNotNullAndIsActiveTrueOrderBySellingDisplayOrderAsc())
                .thenThrow(new RuntimeException("Failed to fetch selling data"));

        assertThatThrownBy(() -> productDetailsService.getTopSellingProducts())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to fetch selling data");
        verify(productRepository).findBySellingDisplayOrderIsNotNullAndIsActiveTrueOrderBySellingDisplayOrderAsc();
    }

    //---------------------------------------------------test Get Products by Category ID -----------------------------------------------------------------------------
    @Test
    void testGetProductsByCategoryId_Success() {
        ProductCategory category = ProductCategory.builder().productCategoryId(1L).productCategoryName("Electronics").build();

        List<ProductDetails> productsInCategory = List.of(
                ProductDetails.builder().productDetailsId(1L).productName("Product A").isActive(true).productCategory(category).build(),
                ProductDetails.builder().productDetailsId(2L).productName("Product B").isActive(true).productCategory(category).build()
        );

        when(productRepository.findByProductCategory_ProductCategoryIdAndIsActiveTrue(1L)).thenReturn(productsInCategory);

        List<ProductDetailsDTO> result = productDetailsService.getProductsByCategory(1L);

        assertEquals(2, result.size());
        assertEquals("Product A", result.get(0).getProductName());
        assertEquals("Product B", result.get(1).getProductName());
        verify(productRepository).findByProductCategory_ProductCategoryIdAndIsActiveTrue(1L);
    }

    @Test
    void testGetProductsByCategoryId_NoProductsFound() {
        when(productRepository.findByProductCategory_ProductCategoryIdAndIsActiveTrue(1L)).thenReturn(Collections.emptyList());

        List<ProductDetailsDTO> result = productDetailsService.getProductsByCategory(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository).findByProductCategory_ProductCategoryIdAndIsActiveTrue(1L);
    }

    @Test
    void testGetProductsByCategoryId_ShouldThrowRuntimeException_WhenUnexpectedRuntimeOccurs() {
        when(productRepository.findByProductCategory_ProductCategoryIdAndIsActiveTrue(1L))
                .thenThrow(new RuntimeException("Database error during category product fetch"));

        assertThatThrownBy(() -> productDetailsService.getProductsByCategory(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database error during category product fetch");
        verify(productRepository).findByProductCategory_ProductCategoryIdAndIsActiveTrue(1L);
    }

    //============================== NEW: Test Update Trending Product Display Orders ===================================
    @Test
    void testUpdateTrendingProductDisplayOrders_ShouldThrowRuntimeException_WhenFindingProductsToClear() {
        List<ProductOrderUpdateDTO> updates = Arrays.asList(
                new ProductOrderUpdateDTO(1L, 1),
                new ProductOrderUpdateDTO(2L, 2)
        );

        // Simulate a RuntimeException when trying to find products to clear their order
        when(productRepository.findByTrendingDisplayOrderIsNotNull())
                .thenThrow(new RuntimeException("DB error during trending product order clear fetch"));

        assertThatThrownBy(() -> productDetailsService.updateTrendingProductDisplayOrders(updates))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB error during trending product order clear fetch");

        verify(productRepository).findByTrendingDisplayOrderIsNotNull();
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    void testUpdateTrendingProductDisplayOrders_ShouldThrowRuntimeException_WhenSavingClearedProduct() {
        ProductDetails productToClear = ProductDetails.builder().productDetailsId(10L).trendingDisplayOrder(5).build();
        List<ProductDetails> productsToClear = List.of(productToClear);

        List<ProductOrderUpdateDTO> updates = Collections.emptyList(); // No updates, so all existing trending products will be cleared

        when(productRepository.findByTrendingDisplayOrderIsNotNull()).thenReturn(productsToClear);
        // Simulate an error when saving the product after nullifying its trendingDisplayOrder
        when(productRepository.save(productToClear)).thenThrow(new RuntimeException("DB error when saving cleared trending product"));

        assertThatThrownBy(() -> productDetailsService.updateTrendingProductDisplayOrders(updates))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB error when saving cleared trending product");

        verify(productRepository).findByTrendingDisplayOrderIsNotNull();
        verify(productRepository).save(productToClear); // Verify that save was called
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    void testUpdateTrendingProductDisplayOrders_ShouldThrowRuntimeException_WhenFindingProductForUpdate() {
        List<ProductOrderUpdateDTO> updates = Arrays.asList(
                new ProductOrderUpdateDTO(1L, 1),
                new ProductOrderUpdateDTO(2L, 2)
        );

        // Ensure no products are returned for clearing trending order to focus on the update loop
        when(productRepository.findByTrendingDisplayOrderIsNotNull()).thenReturn(Collections.emptyList());

        // Simulate a RuntimeException when trying to find a product for updating its order
        when(productRepository.findById(1L)).thenThrow(new RuntimeException("DB error finding product for trending update"));

        assertThatThrownBy(() -> productDetailsService.updateTrendingProductDisplayOrders(updates))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB error finding product for trending update");

        verify(productRepository).findByTrendingDisplayOrderIsNotNull();
        verify(productRepository).findById(1L);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    void testUpdateTrendingProductDisplayOrders_ShouldThrowRuntimeException_WhenSavingUpdatedProduct() {
        ProductDetails existingProduct = ProductDetails.builder().productDetailsId(1L).build();
        List<ProductOrderUpdateDTO> updates = Arrays.asList(
                new ProductOrderUpdateDTO(1L, 10)
        );

        // Ensure no products are returned for clearing trending order
        when(productRepository.findByTrendingDisplayOrderIsNotNull()).thenReturn(Collections.emptyList());
        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        // Simulate a RuntimeException when saving the updated product
        when(productRepository.save(any(ProductDetails.class))).thenThrow(new RuntimeException("DB error saving updated trending product"));

        assertThatThrownBy(() -> productDetailsService.updateTrendingProductDisplayOrders(updates))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB error saving updated trending product");

        verify(productRepository).findByTrendingDisplayOrderIsNotNull();
        verify(productRepository).findById(1L);
        verify(productRepository).save(existingProduct); // Verify save was called
        verifyNoMoreInteractions(productRepository);
    }

    //============================== NEW: Test Update Selling Product Display Orders ===================================
    @Test
    void testUpdateSellingProductDisplayOrders_ShouldThrowRuntimeException_WhenFindingProductsToClear() {
        List<ProductOrderUpdateDTO> updates = Arrays.asList(
                new ProductOrderUpdateDTO(1L, 1),
                new ProductOrderUpdateDTO(2L, 2)
        );

        // Simulate a RuntimeException when trying to find products to clear their order
        when(productRepository.findBySellingDisplayOrderIsNotNull())
                .thenThrow(new RuntimeException("DB error during selling product order clear fetch"));

        assertThatThrownBy(() -> productDetailsService.updateSellingProductDisplayOrders(updates))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB error during selling product order clear fetch");

        verify(productRepository).findBySellingDisplayOrderIsNotNull();
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    void testUpdateSellingProductDisplayOrders_ShouldThrowRuntimeException_WhenSavingClearedProduct() {
        ProductDetails productToClear = ProductDetails.builder().productDetailsId(10L).sellingDisplayOrder(5).build();
        List<ProductDetails> productsToClear = List.of(productToClear);

        List<ProductOrderUpdateDTO> updates = Collections.emptyList(); // No updates, so all existing selling products will be cleared

        when(productRepository.findBySellingDisplayOrderIsNotNull()).thenReturn(productsToClear);
        // Simulate an error when saving the product after nullifying its sellingDisplayOrder
        when(productRepository.save(productToClear)).thenThrow(new RuntimeException("DB error when saving cleared selling product"));

        assertThatThrownBy(() -> productDetailsService.updateSellingProductDisplayOrders(updates))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB error when saving cleared selling product");

        verify(productRepository).findBySellingDisplayOrderIsNotNull();
        verify(productRepository).save(productToClear);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    void testUpdateSellingProductDisplayOrders_ShouldThrowRuntimeException_WhenFindingProductForUpdate() {
        List<ProductOrderUpdateDTO> updates = Arrays.asList(
                new ProductOrderUpdateDTO(1L, 1),
                new ProductOrderUpdateDTO(2L, 2)
        );

        // Ensure no products are returned for clearing selling order to focus on the update loop
        when(productRepository.findBySellingDisplayOrderIsNotNull()).thenReturn(Collections.emptyList());

        // Simulate a RuntimeException when trying to find a product for updating its order
        when(productRepository.findById(1L)).thenThrow(new RuntimeException("DB error finding product for selling update"));

        assertThatThrownBy(() -> productDetailsService.updateSellingProductDisplayOrders(updates))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB error finding product for selling update");

        verify(productRepository).findBySellingDisplayOrderIsNotNull();
        verify(productRepository).findById(1L);
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    void testUpdateSellingProductDisplayOrders_ShouldThrowRuntimeException_WhenSavingUpdatedProduct() {
        ProductDetails existingProduct = ProductDetails.builder().productDetailsId(1L).build();
        List<ProductOrderUpdateDTO> updates = Arrays.asList(
                new ProductOrderUpdateDTO(1L, 10)
        );

        // Ensure no products are returned for clearing selling order
        when(productRepository.findBySellingDisplayOrderIsNotNull()).thenReturn(Collections.emptyList());
        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        // Simulate a RuntimeException when saving the updated product
        when(productRepository.save(any(ProductDetails.class))).thenThrow(new RuntimeException("DB error saving updated selling product"));

        assertThatThrownBy(() -> productDetailsService.updateSellingProductDisplayOrders(updates))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB error saving updated selling product");

        verify(productRepository).findBySellingDisplayOrderIsNotNull();
        verify(productRepository).findById(1L);
        verify(productRepository).save(existingProduct);
        verifyNoMoreInteractions(productRepository);
    }
}