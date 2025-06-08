package com.application.controller;

import com.application.dto.ProductDetailsDTO;
import com.application.dto.ProductOrderUpdateDTO;

import com.application.service.ProductDetailsService;
import com.application.service.impl.ImageUploadService; // Import ImageUploadService
import com.application.util.AppConstants;
import com.fasterxml.jackson.databind.ObjectMapper; // For converting DTOs to JSON
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile; // For simulating file uploads
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*; // Import all request builders
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductDetailsControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductDetailsService productDetailsService;

    @Mock // Mock the ImageUploadService
    private ImageUploadService imageUploadService;

    @InjectMocks
    private ProductDetailsController productDetailsController;

    private ObjectMapper objectMapper;

    private ProductDetailsDTO validProductDTO;
    private ProductDetailsDTO existingProductDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper(); // Initialize ObjectMapper
        mockMvc = MockMvcBuilders.standaloneSetup(productDetailsController).build(); // Build MockMvc with the controller

        validProductDTO = ProductDetailsDTO.builder()
                .productDetailsId(1L)
                .productName("Smartphone")
                .productQuantity(100)
                .price(BigDecimal.valueOf(999.99))
                .productCategoryId(1L)
                .productCategoryName("Electronics")
                .isActive(true)
                .build();

        existingProductDTO = ProductDetailsDTO.builder()
                .productDetailsId(2L)
                .productName("Laptop")
                .productQuantity(50)
                .price(BigDecimal.valueOf(1499.99))
                .productCategoryId(1L)
                .productCategoryName("Electronics")
                .isActive(true)
                .build();
    }

    // --- Create New Product ---

    @Test
    void createNewProduct_ShouldReturnCreated_WhenValidInputNoImage() throws Exception {
        ProductDetailsDTO productToCreate = ProductDetailsDTO.builder()
                .productName("New Gadget")
                .productQuantity(50)
                .price(BigDecimal.valueOf(100.00))
                .productCategoryId(1L)
                .productCategoryName("Electronics")
                .isActive(true)
                .build();
        // Manually create a new DTO for the simulated return value with ID
        ProductDetailsDTO createdProduct = ProductDetailsDTO.builder()
                .productDetailsId(3L)
                .productName("New Gadget")
                .productQuantity(50)
                .price(BigDecimal.valueOf(100.00))
                .productCategoryId(1L)
                .productCategoryName("Electronics")
                .isActive(true)
                .imageUrl(null) // Ensure explicitly null as no image is provided
                .trendingDisplayOrder(null)
                .sellingDisplayOrder(null)
                .build();

        when(productDetailsService.createProduct(any(ProductDetailsDTO.class))).thenReturn(createdProduct);

        mockMvc.perform(multipart("/api/products")
                        .file(new MockMultipartFile("product", "", "application/json", objectMapper.writeValueAsBytes(productToCreate)))
                        .contentType(MediaType.MULTIPART_FORM_DATA)) // Important for multipart
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productName").value("New Gadget"));

        verify(productDetailsService, times(1)).createProduct(argThat(dto ->
                dto.getProductName().equals("New Gadget") &&
                        dto.getImageUrl() == null && // Assert image URL is null
                        dto.getTrendingDisplayOrder() == null && // Assert trending order is null
                        dto.getSellingDisplayOrder() == null // Assert selling order is null
        ));
        verifyNoInteractions(imageUploadService); // No image provided
    }

    @Test
    void createNewProduct_ShouldReturnCreated_WhenValidInputWithImage() throws Exception {
        ProductDetailsDTO productToCreate = ProductDetailsDTO.builder()
                .productName("Camera")
                .productQuantity(20)
                .price(BigDecimal.valueOf(500.00))
                .productCategoryId(2L)
                .productCategoryName("Photography")
                .isActive(true)
                .build();
        // Manually create a new DTO for the simulated return value with ID and image URL
        ProductDetailsDTO createdProduct = ProductDetailsDTO.builder()
                .productDetailsId(4L)
                .productName("Camera")
                .productQuantity(20)
                .price(BigDecimal.valueOf(500.00))
                .productCategoryId(2L)
                .productCategoryName("Photography")
                .isActive(true)
                .imageUrl("http://example.com/camera.jpg")
                .trendingDisplayOrder(null)
                .sellingDisplayOrder(null)
                .build();


        MockMultipartFile imageFile = new MockMultipartFile("image", "camera.jpg", MediaType.IMAGE_JPEG_VALUE, "image content".getBytes());

        when(imageUploadService.uploadImage(any(MockMultipartFile.class))).thenReturn("http://example.com/camera.jpg");
        when(productDetailsService.createProduct(any(ProductDetailsDTO.class))).thenReturn(createdProduct);

        mockMvc.perform(multipart("/api/products")
                        .file(new MockMultipartFile("product", "", "application/json", objectMapper.writeValueAsBytes(productToCreate)))
                        .file(imageFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productName").value("Camera"))
                .andExpect(jsonPath("$.imageUrl").value("http://example.com/camera.jpg"));

        verify(imageUploadService, times(1)).uploadImage(any(MockMultipartFile.class));
        verify(productDetailsService, times(1)).createProduct(argThat(dto ->
                dto.getProductName().equals("Camera") &&
                        dto.getImageUrl().equals("http://example.com/camera.jpg") && // Assert image URL is set
                        dto.getTrendingDisplayOrder() == null && // Assert trending order is null
                        dto.getSellingDisplayOrder() == null // Assert selling order is null
        ));
    }

    @Test
    void createNewProduct_ShouldReturnBadRequest_WhenInvalidInput() throws Exception {
        ProductDetailsDTO invalidProductDTO = ProductDetailsDTO.builder()
                .productName("") // Invalid: empty name
                .productQuantity(10)
                .price(BigDecimal.ZERO) // Invalid: price too low
                .productCategoryId(1L)
                .build();

        // Directly throwing IllegalArgumentException from service for simplified testing,
        // though @Valid would typically catch this before service.
        when(productDetailsService.createProduct(any(ProductDetailsDTO.class)))
                .thenThrow(new IllegalArgumentException("Product name is required"));

        mockMvc.perform(multipart("/api/products")
                        .file(new MockMultipartFile("product", "", "application/json", objectMapper.writeValueAsBytes(invalidProductDTO)))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid input: Product name is required")));

        verify(productDetailsService).createProduct(any(ProductDetailsDTO.class));
        verifyNoInteractions(imageUploadService);
    }

    @Test
    void createNewProduct_ShouldReturnConflict_WhenProductAlreadyExists() throws Exception {
        ProductDetailsDTO productToCreate = ProductDetailsDTO.builder()
                .productName("Existing Product")
                .productQuantity(10)
                .price(BigDecimal.TEN)
                .productCategoryId(1L)
                .build();

        when(productDetailsService.createProduct(any(ProductDetailsDTO.class)))
                .thenThrow(new RuntimeException("Product with name 'Existing Product' already exists."));

        mockMvc.perform(multipart("/api/products")
                        .file(new MockMultipartFile("product", "", "application/json", objectMapper.writeValueAsBytes(productToCreate)))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isConflict())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Product with name 'Existing Product' already exists.")));

        verify(productDetailsService).createProduct(any(ProductDetailsDTO.class));
        verifyNoInteractions(imageUploadService);
    }

    @Test
    void createNewProduct_ShouldReturnConflict_WhenDataIntegrityViolation() throws Exception {
        ProductDetailsDTO productToCreate = ProductDetailsDTO.builder()
                .productName("Product B")
                .productQuantity(10)
                .price(BigDecimal.TEN)
                .productCategoryId(1L)
                .build();

        when(productDetailsService.createProduct(any(ProductDetailsDTO.class)))
                .thenThrow(new DataIntegrityViolationException("Constraint violation: unique name"));

        mockMvc.perform(multipart("/api/products")
                        .file(new MockMultipartFile("product", "", "application/json", objectMapper.writeValueAsBytes(productToCreate)))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isConflict())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Conflict: DB constraint violated")));

        verify(productDetailsService).createProduct(any(ProductDetailsDTO.class));
        verifyNoInteractions(imageUploadService);
    }

    @Test
    void createNewProduct_ShouldReturnInternalServerError_WhenImageUploadFails() throws Exception {
        ProductDetailsDTO productToCreate = ProductDetailsDTO.builder()
                .productName("Tablet")
                .productQuantity(5)
                .price(BigDecimal.valueOf(300.00))
                .productCategoryId(1L)
                .build();
        MockMultipartFile imageFile = new MockMultipartFile("image", "tablet.jpg", MediaType.IMAGE_JPEG_VALUE, "image content".getBytes());

        when(imageUploadService.uploadImage(any(MockMultipartFile.class))).thenThrow(new IOException("Storage full"));

        mockMvc.perform(multipart("/api/products")
                        .file(new MockMultipartFile("product", "", "application/json", objectMapper.writeValueAsBytes(productToCreate)))
                        .file(imageFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Image upload failed: Storage full")));

        verify(imageUploadService).uploadImage(any(MockMultipartFile.class));
        verifyNoInteractions(productDetailsService); // Service should not be called if image upload fails
    }

    @Test
    void createNewProduct_ShouldReturnInternalServerError_WhenGenericRuntimeError() throws Exception {
        ProductDetailsDTO productToCreate = ProductDetailsDTO.builder()
                .productName("Mystery Item")
                .productQuantity(1)
                .price(BigDecimal.ONE)
                .productCategoryId(1L)
                .build();

        when(productDetailsService.createProduct(any(ProductDetailsDTO.class)))
                .thenThrow(new RuntimeException("Unexpected server error"));

        mockMvc.perform(multipart("/api/products")
                        .file(new MockMultipartFile("product", "", "application/json", objectMapper.writeValueAsBytes(productToCreate)))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(AppConstants.FAILED + ": Unexpected server error")));

        verify(productDetailsService).createProduct(any(ProductDetailsDTO.class));
    }


    // --- Update Product By ProductId ---

    @Test
    void updateProductByProductId_ShouldReturnOk_WhenValidInputNoImageChange() throws Exception {
        Long productId = 1L;
        // Manually create updatedProductDTO
        ProductDetailsDTO updatedProductDTO = ProductDetailsDTO.builder()
                .productDetailsId(1L)
                .productName("Smartphone Pro")
                .productQuantity(100) // Keep original quantity or set updated
                .price(BigDecimal.valueOf(999.99)) // Keep original price or set updated
                .productCategoryId(1L)
                .productCategoryName("Electronics")
                .isActive(false) // Changed isActive
                .imageUrl("existing-image.jpg") // Keep existing image
                .trendingDisplayOrder(1) // Simulate original value, should be nullified
                .sellingDisplayOrder(2) // Simulate original value, should be nullified
                .build();

        // Manually create serviceReturnedDTO as it would be after controller processing
        ProductDetailsDTO serviceReturnedDTO = ProductDetailsDTO.builder()
                .productDetailsId(1L)
                .productName("Smartphone Pro")
                .productQuantity(100)
                .price(BigDecimal.valueOf(999.99))
                .productCategoryId(1L)
                .productCategoryName("Electronics")
                .isActive(false)
                .imageUrl("existing-image.jpg")
                .trendingDisplayOrder(null) // Expect null after controller processing
                .sellingDisplayOrder(null) // Expect null after controller processing
                .build();


        when(productDetailsService.updateProductByProductId(eq(productId), any(ProductDetailsDTO.class)))
                .thenReturn(serviceReturnedDTO);

        mockMvc.perform(multipart("/api/products/{productId}", productId)
                        .file(new MockMultipartFile("product", "", "application/json", objectMapper.writeValueAsBytes(updatedProductDTO)))
                        .with(request -> { // Simulate PUT for multipart
                            request.setMethod("PUT");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Smartphone Pro"))
                .andExpect(jsonPath("$.isActive").value(false))
                .andExpect(jsonPath("$.imageUrl").value("existing-image.jpg"))
                .andExpect(jsonPath("$.trendingDisplayOrder").doesNotExist()) // Assert it's not present or null
                .andExpect(jsonPath("$.sellingDisplayOrder").doesNotExist()); // Assert it's not present or null


        verify(productDetailsService, times(1)).updateProductByProductId(eq(productId), argThat(dto ->
                dto.getProductName().equals("Smartphone Pro") &&
                        dto.getImageUrl().equals("existing-image.jpg") && // Image URL passed as is
                        dto.getTrendingDisplayOrder() == null && // Assert it was set to null by controller
                        dto.getSellingDisplayOrder() == null // Assert it was set to null by controller
        ));
        verifyNoInteractions(imageUploadService);
    }

    @Test
    void updateProductByProductId_ShouldReturnOk_WhenValidInputWithNewImage() throws Exception {
        Long productId = 1L;
        // Manually create productToUpdate
        ProductDetailsDTO productToUpdate = ProductDetailsDTO.builder()
                .productDetailsId(1L)
                .productName("Smartphone X")
                .productQuantity(validProductDTO.getProductQuantity())
                .price(validProductDTO.getPrice())
                .productCategoryId(validProductDTO.getProductCategoryId())
                .productCategoryName(validProductDTO.getProductCategoryName())
                .isActive(true)
                .imageUrl(null) // Indicate image should be replaced
                .build();
        // Manually create updatedProduct
        ProductDetailsDTO updatedProduct = ProductDetailsDTO.builder()
                .productDetailsId(1L)
                .productName("Smartphone X")
                .productQuantity(validProductDTO.getProductQuantity())
                .price(validProductDTO.getPrice())
                .productCategoryId(validProductDTO.getProductCategoryId())
                .productCategoryName(validProductDTO.getProductCategoryName())
                .isActive(true)
                .imageUrl("http://newimage.com/spx.jpg")
                .trendingDisplayOrder(null)
                .sellingDisplayOrder(null)
                .build();

        MockMultipartFile newImageFile = new MockMultipartFile("image", "new_image.jpg", MediaType.IMAGE_JPEG_VALUE, "new image content".getBytes());

        when(imageUploadService.uploadImage(any(MockMultipartFile.class))).thenReturn("http://newimage.com/spx.jpg");
        when(productDetailsService.updateProductByProductId(eq(productId), any(ProductDetailsDTO.class))).thenReturn(updatedProduct);

        mockMvc.perform(multipart("/api/products/{productId}", productId)
                        .file(new MockMultipartFile("product", "", "application/json", objectMapper.writeValueAsBytes(productToUpdate)))
                        .file(newImageFile)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value("http://newimage.com/spx.jpg"));

        verify(imageUploadService, times(1)).uploadImage(any(MockMultipartFile.class));
        verify(productDetailsService, times(1)).updateProductByProductId(eq(productId), argThat(dto ->
                dto.getImageUrl().equals("http://newimage.com/spx.jpg") && // Assert new image URL is set
                        dto.getTrendingDisplayOrder() == null &&
                        dto.getSellingDisplayOrder() == null
        ));
    }

    @Test
    void updateProductByProductId_ShouldReturnOk_WhenValidInputWithImageRemoved() throws Exception {
        Long productId = 1L;
        // Manually create productToUpdate
        ProductDetailsDTO productToUpdate = ProductDetailsDTO.builder()
                .productDetailsId(1L)
                .productName("Smartphone Base")
                .productQuantity(validProductDTO.getProductQuantity())
                .price(validProductDTO.getPrice())
                .productCategoryId(validProductDTO.getProductCategoryId())
                .productCategoryName(validProductDTO.getProductCategoryName())
                .isActive(validProductDTO.getIsActive())
                .imageUrl("") // Simulate removing image (empty string)
                .build();
        // Manually create updatedProduct
        ProductDetailsDTO updatedProduct = ProductDetailsDTO.builder()
                .productDetailsId(1L)
                .productName("Smartphone Base")
                .productQuantity(validProductDTO.getProductQuantity())
                .price(validProductDTO.getPrice())
                .productCategoryId(validProductDTO.getProductCategoryId())
                .productCategoryName(validProductDTO.getProductCategoryName())
                .isActive(validProductDTO.getIsActive())
                .imageUrl(null) // Service returns null for removed image
                .trendingDisplayOrder(null)
                .sellingDisplayOrder(null)
                .build();

        when(productDetailsService.updateProductByProductId(eq(productId), any(ProductDetailsDTO.class))).thenReturn(updatedProduct);

        mockMvc.perform(multipart("/api/products/{productId}", productId)
                        .file(new MockMultipartFile("product", "", "application/json", objectMapper.writeValueAsBytes(productToUpdate)))
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").doesNotExist()); // Assert image URL is removed/null

        verify(productDetailsService, times(1)).updateProductByProductId(eq(productId), argThat(dto ->
                dto.getImageUrl() == null && // Assert image URL was set to null by controller
                        dto.getTrendingDisplayOrder() == null &&
                        dto.getSellingDisplayOrder() == null
        ));
        verifyNoInteractions(imageUploadService);
    }

    @Test
    void updateProductByProductId_ShouldReturnNotFound_WhenProductNotFound() throws Exception {
        Long productId = 99L;
        // Manually create productToUpdate
        ProductDetailsDTO productToUpdate = ProductDetailsDTO.builder()
                .productDetailsId(productId)
                .productName(validProductDTO.getProductName())
                .productQuantity(validProductDTO.getProductQuantity())
                .price(validProductDTO.getPrice())
                .productCategoryId(validProductDTO.getProductCategoryId())
                .productCategoryName(validProductDTO.getProductCategoryName())
                .isActive(validProductDTO.getIsActive())
                .build();

        when(productDetailsService.updateProductByProductId(eq(productId), any(ProductDetailsDTO.class)))
                .thenThrow(new RuntimeException(AppConstants.PRODUCT_NOT_FOUND + productId));

        mockMvc.perform(multipart("/api/products/{productId}", productId)
                        .file(new MockMultipartFile("product", "", "application/json", objectMapper.writeValueAsBytes(productToUpdate)))
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isNotFound())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(AppConstants.PRODUCT_NOT_FOUND + productId)));

        verify(productDetailsService).updateProductByProductId(eq(productId), any(ProductDetailsDTO.class));
        verifyNoInteractions(imageUploadService);
    }

    @Test
    void updateProductByProductId_ShouldReturnNotFound_WhenCategoryNotFound() throws Exception {
        Long productId = 1L;
        // Manually create productToUpdate with non-existent category
        ProductDetailsDTO productToUpdate = ProductDetailsDTO.builder()
                .productDetailsId(1L)
                .productName(validProductDTO.getProductName())
                .productQuantity(validProductDTO.getProductQuantity())
                .price(validProductDTO.getPrice())
                .productCategoryId(99L) // Non-existent category
                .productCategoryName(validProductDTO.getProductCategoryName())
                .isActive(validProductDTO.getIsActive())
                .build();


        when(productDetailsService.updateProductByProductId(eq(productId), any(ProductDetailsDTO.class)))
                .thenThrow(new RuntimeException(AppConstants.CATEGORY_NOT_FOUND + "99"));

        mockMvc.perform(multipart("/api/products/{productId}", productId)
                        .file(new MockMultipartFile("product", "", "application/json", objectMapper.writeValueAsBytes(productToUpdate)))
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isNotFound())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(AppConstants.CATEGORY_NOT_FOUND + "99")));

        verify(productDetailsService).updateProductByProductId(eq(productId), any(ProductDetailsDTO.class));
        verifyNoInteractions(imageUploadService);
    }

    @Test
    void updateProductByProductId_ShouldReturnConflict_WhenProductNameExists() throws Exception {
        Long productId = 1L;
        // Manually create productToUpdate with conflicting name
        ProductDetailsDTO productToUpdate = ProductDetailsDTO.builder()
                .productDetailsId(1L)
                .productName("Another Product") // Conflicting name
                .productQuantity(validProductDTO.getProductQuantity())
                .price(validProductDTO.getPrice())
                .productCategoryId(validProductDTO.getProductCategoryId())
                .productCategoryName(validProductDTO.getProductCategoryName())
                .isActive(validProductDTO.getIsActive())
                .build();


        when(productDetailsService.updateProductByProductId(eq(productId), any(ProductDetailsDTO.class)))
                .thenThrow(new RuntimeException("Product with name 'Another Product' already exists."));

        mockMvc.perform(multipart("/api/products/{productId}", productId)
                        .file(new MockMultipartFile("product", "", "application/json", objectMapper.writeValueAsBytes(productToUpdate)))
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isConflict())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Product with name 'Another Product' already exists.")));

        verify(productDetailsService).updateProductByProductId(eq(productId), any(ProductDetailsDTO.class));
        verifyNoInteractions(imageUploadService);
    }

    @Test
    void updateProductByProductId_ShouldReturnBadRequest_WhenInvalidInput() throws Exception {
        Long productId = 1L;
        // Manually create invalidProductDTO
        ProductDetailsDTO invalidProductDTO = ProductDetailsDTO.builder()
                .productDetailsId(1L)
                .productName("") // Invalid name
                .productQuantity(validProductDTO.getProductQuantity())
                .price(validProductDTO.getPrice())
                .productCategoryId(validProductDTO.getProductCategoryId())
                .productCategoryName(validProductDTO.getProductCategoryName())
                .isActive(validProductDTO.getIsActive())
                .build();

    }

    @Test
    void updateProductByProductId_ShouldReturnInternalServerError_WhenImageUploadFails() throws Exception {
        Long productId = 1L;
        // Manually create productToUpdate
        ProductDetailsDTO productToUpdate = ProductDetailsDTO.builder()
                .productDetailsId(1L)
                .productName("Smartwatch")
                .productQuantity(validProductDTO.getProductQuantity())
                .price(validProductDTO.getPrice())
                .productCategoryId(validProductDTO.getProductCategoryId())
                .productCategoryName(validProductDTO.getProductCategoryName())
                .isActive(validProductDTO.getIsActive())
                .build();
        MockMultipartFile imageFile = new MockMultipartFile("image", "smartwatch.jpg", MediaType.IMAGE_JPEG_VALUE, "image content".getBytes());

        when(imageUploadService.uploadImage(any(MockMultipartFile.class))).thenThrow(new IOException("Network error"));

        mockMvc.perform(multipart("/api/products/{productId}", productId)
                        .file(new MockMultipartFile("product", "", "application/json", objectMapper.writeValueAsBytes(productToUpdate)))
                        .file(imageFile)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Image upload failed: Network error")));

        verify(imageUploadService).uploadImage(any(MockMultipartFile.class));
        verifyNoInteractions(productDetailsService);
    }

    @Test
    void updateProductByProductId_ShouldReturnInternalServerError_WhenUnknownRuntimeError() throws Exception {
        Long productId = 1L;
        // Manually create productToUpdate
        ProductDetailsDTO productToUpdate = ProductDetailsDTO.builder()
                .productDetailsId(1L)
                .productName("Updated Item")
                .productQuantity(validProductDTO.getProductQuantity())
                .price(validProductDTO.getPrice())
                .productCategoryId(validProductDTO.getProductCategoryId())
                .productCategoryName(validProductDTO.getProductCategoryName())
                .isActive(validProductDTO.getIsActive())
                .build();

        when(productDetailsService.updateProductByProductId(eq(productId), any(ProductDetailsDTO.class)))
                .thenThrow(new RuntimeException("A service layer unexpected error"));

        mockMvc.perform(multipart("/api/products/{productId}", productId)
                        .file(new MockMultipartFile("product", "", "application/json", objectMapper.writeValueAsBytes(productToUpdate)))
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(AppConstants.FAILED + ": A service layer unexpected error")));

        verify(productDetailsService).updateProductByProductId(eq(productId), any(ProductDetailsDTO.class));
    }


    // --- Get Product Details By ProductId ---

    @Test
    void getProductDetailsByProductId_ShouldReturnOk_WhenProductExists() throws Exception {
        when(productDetailsService.getProductByProductId(anyLong())).thenReturn(validProductDTO);

        mockMvc.perform(get("/api/products/{productId}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productDetailsId").value(1L))
                .andExpect(jsonPath("$.productName").value("Smartphone"));

        verify(productDetailsService, times(1)).getProductByProductId(1L);
    }

    @Test
    void getProductDetailsByProductId_ShouldReturnNotFound_WhenProductNotFound() throws Exception {
        when(productDetailsService.getProductByProductId(anyLong()))
                .thenThrow(new RuntimeException(AppConstants.PRODUCT_NOT_FOUND + 1L));

        mockMvc.perform(get("/api/products/{productId}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(AppConstants.PRODUCT_NOT_FOUND + 1L)));

        verify(productDetailsService, times(1)).getProductByProductId(1L);
    }

    @Test
    void getProductDetailsByProductId_ShouldReturnBadRequest_WhenInvalidId() throws Exception {
        when(productDetailsService.getProductByProductId(anyLong()))
                .thenThrow(new IllegalArgumentException("ID must be positive"));

        mockMvc.perform(get("/api/products/{productId}", -1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid product ID: ID must be positive")));

        verify(productDetailsService, times(1)).getProductByProductId(-1L);
    }

    @Test
    void getProductDetailsByProductId_ShouldReturnInternalServerError_WhenUnexpectedRuntimeError() throws Exception {
        when(productDetailsService.getProductByProductId(anyLong()))
                .thenThrow(new RuntimeException("Unexpected DB error"));

        mockMvc.perform(get("/api/products/{productId}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(AppConstants.FAILED + ": Unexpected DB error")));

        verify(productDetailsService, times(1)).getProductByProductId(1L);
    }

    // --- Get All Product Details ---

    @Test
    void getAllProductDetails_ShouldReturnOk_WhenProductsExist() throws Exception {
        List<ProductDetailsDTO> products = Arrays.asList(validProductDTO, existingProductDTO);
        when(productDetailsService.getAllProducts()).thenReturn(products);

        mockMvc.perform(get("/api/products")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].productName").value("Smartphone"));

        verify(productDetailsService, times(1)).getAllProducts();
    }

    @Test
    void getAllProductDetails_ShouldReturnEmptyList_WhenNoProductsExist() throws Exception {
        when(productDetailsService.getAllProducts()).thenReturn(List.of());

        mockMvc.perform(get("/api/products")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(productDetailsService, times(1)).getAllProducts();
    }

    @Test
    void getAllProductDetails_ShouldReturnInternalServerError_WhenUnexpectedError() throws Exception {
        when(productDetailsService.getAllProducts())
                .thenThrow(new RuntimeException("Unexpected error fetching all products"));

        mockMvc.perform(get("/api/products")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(AppConstants.FAILED + ": Unexpected error fetching all products")));

        verify(productDetailsService, times(1)).getAllProducts();
    }


    // --- Get Top Trending Products ---

    @Test
    void getTopTrendingProducts_ShouldReturnOk_WhenTrendingProductsExist() throws Exception {
        // Manually create trending products
        List<ProductDetailsDTO> trendingProducts = Arrays.asList(
                ProductDetailsDTO.builder().productDetailsId(validProductDTO.getProductDetailsId()).productName(validProductDTO.getProductName()).productQuantity(validProductDTO.getProductQuantity()).price(validProductDTO.getPrice()).productCategoryId(validProductDTO.getProductCategoryId()).productCategoryName(validProductDTO.getProductCategoryName()).isActive(validProductDTO.getIsActive()).imageUrl(validProductDTO.getImageUrl()).trendingDisplayOrder(1).sellingDisplayOrder(validProductDTO.getSellingDisplayOrder()).build(),
                ProductDetailsDTO.builder().productDetailsId(existingProductDTO.getProductDetailsId()).productName(existingProductDTO.getProductName()).productQuantity(existingProductDTO.getProductQuantity()).price(existingProductDTO.getPrice()).productCategoryId(existingProductDTO.getProductCategoryId()).productCategoryName(existingProductDTO.getProductCategoryName()).isActive(existingProductDTO.getIsActive()).imageUrl(existingProductDTO.getImageUrl()).trendingDisplayOrder(2).sellingDisplayOrder(existingProductDTO.getSellingDisplayOrder()).build()
        );
        when(productDetailsService.getTopTrendingProducts()).thenReturn(trendingProducts);

        mockMvc.perform(get("/api/products/trending")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].productName").value("Smartphone"));

        verify(productDetailsService, times(1)).getTopTrendingProducts();
    }

    @Test
    void getTopTrendingProducts_ShouldReturnInternalServerError_WhenUnexpectedError() throws Exception {
        when(productDetailsService.getTopTrendingProducts())
                .thenThrow(new RuntimeException("Failed to fetch trending products"));

        mockMvc.perform(get("/api/products/trending")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(AppConstants.FAILED + ": Failed to fetch trending products")));

        verify(productDetailsService, times(1)).getTopTrendingProducts();
    }

    // --- Get Top Selling Products ---

    @Test
    void getTopSellingProducts_ShouldReturnOk_WhenSellingProductsExist() throws Exception {
        // Manually create selling products
        List<ProductDetailsDTO> sellingProducts = Arrays.asList(
                ProductDetailsDTO.builder().productDetailsId(validProductDTO.getProductDetailsId()).productName(validProductDTO.getProductName()).productQuantity(validProductDTO.getProductQuantity()).price(validProductDTO.getPrice()).productCategoryId(validProductDTO.getProductCategoryId()).productCategoryName(validProductDTO.getProductCategoryName()).isActive(validProductDTO.getIsActive()).imageUrl(validProductDTO.getImageUrl()).trendingDisplayOrder(validProductDTO.getTrendingDisplayOrder()).sellingDisplayOrder(1).build(),
                ProductDetailsDTO.builder().productDetailsId(existingProductDTO.getProductDetailsId()).productName(existingProductDTO.getProductName()).productQuantity(existingProductDTO.getProductQuantity()).price(existingProductDTO.getPrice()).productCategoryId(existingProductDTO.getProductCategoryId()).productCategoryName(existingProductDTO.getProductCategoryName()).isActive(existingProductDTO.getIsActive()).imageUrl(existingProductDTO.getImageUrl()).trendingDisplayOrder(existingProductDTO.getTrendingDisplayOrder()).sellingDisplayOrder(2).build()
        );
        when(productDetailsService.getTopSellingProducts()).thenReturn(sellingProducts);

        mockMvc.perform(get("/api/products/selling")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].productName").value("Smartphone"));

        verify(productDetailsService, times(1)).getTopSellingProducts();
    }

    @Test
    void getTopSellingProducts_ShouldReturnInternalServerError_WhenUnexpectedError() throws Exception {
        when(productDetailsService.getTopSellingProducts())
                .thenThrow(new RuntimeException("Failed to fetch selling products"));

        mockMvc.perform(get("/api/products/selling")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(AppConstants.FAILED + ": Failed to fetch selling products")));

        verify(productDetailsService, times(1)).getTopSellingProducts();
    }

    // --- Get Products by Category ID ---

    @Test
    void getProductsByCategory_ShouldReturnOk_WhenProductsExistForCategory() throws Exception {
        Long categoryId = 1L;
        List<ProductDetailsDTO> categoryProducts = Arrays.asList(validProductDTO, existingProductDTO);
        when(productDetailsService.getProductsByCategory(categoryId)).thenReturn(categoryProducts);

        mockMvc.perform(get("/api/products/category/{categoryId}", categoryId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].productCategoryId").value(categoryId));

        verify(productDetailsService, times(1)).getProductsByCategory(categoryId);
    }

    @Test
    void getProductsByCategory_ShouldReturnBadRequest_WhenInvalidCategoryId() throws Exception {
        Long categoryId = -5L;
        when(productDetailsService.getProductsByCategory(categoryId))
                .thenThrow(new IllegalArgumentException("Category ID must be positive"));

        mockMvc.perform(get("/api/products/category/{categoryId}", categoryId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid category ID: Category ID must be positive")));

        verify(productDetailsService, times(1)).getProductsByCategory(categoryId);
    }

    @Test
    void getProductsByCategory_ShouldReturnInternalServerError_WhenUnexpectedError() throws Exception {
        Long categoryId = 1L;
        when(productDetailsService.getProductsByCategory(categoryId))
                .thenThrow(new RuntimeException("Database error fetching category products"));

        mockMvc.perform(get("/api/products/category/{categoryId}", categoryId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(AppConstants.FAILED + ": Database error fetching category products")));

        verify(productDetailsService, times(1)).getProductsByCategory(categoryId);
    }

    // --- Update Trending Product Order ---

    @Test
    void updateTrendingProductOrder_ShouldReturnOk() throws Exception {
        List<ProductOrderUpdateDTO> updates = Arrays.asList(
                new ProductOrderUpdateDTO(1L, 1),
                new ProductOrderUpdateDTO(2L, 2)
        );

        doNothing().when(productDetailsService).updateTrendingProductDisplayOrders(updates);

        mockMvc.perform(put("/api/products/update-trending-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk());

        verify(productDetailsService, times(1)).updateTrendingProductDisplayOrders(updates);
    }

    // --- Update Selling Product Order ---

    @Test
    void updateSellingProductOrder_ShouldReturnOk() throws Exception {
        List<ProductOrderUpdateDTO> updates = Arrays.asList(
                new ProductOrderUpdateDTO(3L, 1),
                new ProductOrderUpdateDTO(4L, 2)
        );

        doNothing().when(productDetailsService).updateSellingProductDisplayOrders(updates);

        mockMvc.perform(put("/api/products/update-selling-order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updates)))
                .andExpect(status().isOk());

        verify(productDetailsService, times(1)).updateSellingProductDisplayOrders(updates);
    }


    // --- Delete Product By ProductId ---

    @Test
    void deleteProductByProductId_ShouldReturnNoContent_WhenSuccessful() throws Exception {
        Long productId = 1L;
        doNothing().when(productDetailsService).deleteProductByProductId(productId);

        mockMvc.perform(delete("/api/products/{productId}", productId))
                .andExpect(status().isNoContent());

        verify(productDetailsService, times(1)).deleteProductByProductId(productId);
    }

    @Test
    void deleteProductByProductId_ShouldReturnNotFound_WhenProductNotFound() throws Exception {
        Long productId = 99L;
        doThrow(new RuntimeException(AppConstants.PRODUCT_NOT_FOUND + productId)) // Match controller's catch for RuntimeException with specific message
                .when(productDetailsService).deleteProductByProductId(productId);

        mockMvc.perform(delete("/api/products/{productId}", productId))
                .andExpect(status().isNotFound())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(AppConstants.PRODUCT_NOT_FOUND + productId)));

        verify(productDetailsService, times(1)).deleteProductByProductId(productId);
    }

    @Test
    void deleteProductByProductId_ShouldReturnInternalServerError_WhenUnexpectedError() throws Exception {
        Long productId = 1L;
        doThrow(new RuntimeException("Generic delete error"))
                .when(productDetailsService).deleteProductByProductId(productId);

        mockMvc.perform(delete("/api/products/{productId}", productId))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(AppConstants.FAILED + ": Generic delete error")));

        verify(productDetailsService, times(1)).deleteProductByProductId(productId);
    }
}