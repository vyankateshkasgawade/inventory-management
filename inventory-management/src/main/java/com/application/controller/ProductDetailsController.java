package com.application.controller;

import com.application.dto.ProductDetailsDTO;
import com.application.dto.ProductOrderUpdateDTO;
import com.application.service.ProductDetailsService;
import com.application.service.impl.ImageUploadService;
import com.application.util.AppConstants;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.io.IOException;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "ProductDetailsController", description = "Operations related to Product Details management")
public class ProductDetailsController {

    private final ProductDetailsService productDetailsService;
    private final ImageUploadService imageUploadService;

    //==================================create New Product===========================================

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new Product", description = "Create a new product in the system with an optional image")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createNewProduct(
            @Valid @RequestPart("product") ProductDetailsDTO productDTO,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        log.info(AppConstants.CONTROLLER_LOG_PREFIX + "Creating new product: {}", productDTO.getProductName());
        try {
            if (image != null && !image.isEmpty()) {
                String imageUrl = imageUploadService.uploadImage(image);
                productDTO.setImageUrl(imageUrl);
            }

            // Explicitly set display orders to null when creating a new product
            // These are managed by separate sequencing operations.
            productDTO.setTrendingDisplayOrder(null);
            productDTO.setSellingDisplayOrder(null);

            ProductDetailsDTO createdProduct = productDetailsService.createProduct(productDTO);
            return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid product creation input: {}", e.getMessage(), e);
            return new ResponseEntity<>(generateErrorResponse("Invalid input: " + e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (DataIntegrityViolationException e) {
            log.warn("Database constraint violated: {}", e.getMessage(), e);
            return new ResponseEntity<>(generateErrorResponse("Conflict: DB constraint violated"), HttpStatus.CONFLICT);
        } catch (IOException e) {
            log.error("Error uploading image for product {}: {}", productDTO.getProductName(), e.getMessage(), e);
            return new ResponseEntity<>(generateErrorResponse("Image upload failed: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (RuntimeException e) {
            if (e.getMessage().startsWith("Product with name")) {
                return new ResponseEntity<>(generateErrorResponse(e.getMessage()), HttpStatus.CONFLICT);
            } else {
                log.error("Error creating product: {}", productDTO.getProductName(), e);
                return new ResponseEntity<>(generateErrorResponse(AppConstants.FAILED + ": " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }


    //=====================================update Product By ProductId===============================================================

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update Product by ID", description = "Update an existing product based on its unique ID")
    @PutMapping(value = "/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProductByProductId(
            @PathVariable Long productId,
            @Valid @RequestPart("product") ProductDetailsDTO productDTO,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {

        log.info(AppConstants.CONTROLLER_LOG_PREFIX + "Updating product with ID {}: {}", productId, productDTO);

        try {
            if (image != null && !image.isEmpty()) {
                String imageUrl = imageUploadService.uploadImage(image);
                productDTO.setImageUrl(imageUrl);
            } else if (productDTO.getImageUrl() == null || productDTO.getImageUrl().isEmpty()) {
                productDTO.setImageUrl(null);
            }

            // Explicitly ensure display orders are not updated from this general product update endpoint.
            // They are managed by separate sequencing operations.
            productDTO.setTrendingDisplayOrder(null);
            productDTO.setSellingDisplayOrder(null);

            log.info("Updating status to: {}", productDTO.getIsActive() ? "Active" : "Inactive");

            ProductDetailsDTO updatedProduct = productDetailsService.updateProductByProductId(productId, productDTO);

            return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
//        } catch (IllegalArgumentException e) {
//            log.warn("Invalid product update input: {}", e.getMessage(), e);
//            return new ResponseEntity<>(generateErrorResponse("Invalid input: " + e.getMessage()), HttpStatus.BAD_REQUEST);

        } catch (IOException e) {
            log.error("Error uploading image for product ID {}: {}", productId, e.getMessage(), e);
            return new ResponseEntity<>(generateErrorResponse("Image upload failed: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (RuntimeException e) {
            String message = e.getMessage();

            if (message.startsWith("Product with name")) {
                return new ResponseEntity<>(generateErrorResponse(message), HttpStatus.CONFLICT);

            } else if (message.startsWith(AppConstants.PRODUCT_NOT_FOUND) || message.startsWith(AppConstants.CATEGORY_NOT_FOUND)) {
                return new ResponseEntity<>(generateErrorResponse(message), HttpStatus.NOT_FOUND);

            } else {
                log.error("Error updating product with ID {}: {}", productId, e.getMessage(), e);
                return new ResponseEntity<>(generateErrorResponse(AppConstants.FAILED + ": " + message), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    //==================================get Product Details By ProductId=================================================

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Get Product Details by ID", description = "Fetch a product by its unique ID")
    @GetMapping("/{productId}")
    public ResponseEntity<?> getProductDetailsByProductId(@PathVariable Long productId) {
        log.info("{}Fetching product by ID: {}", AppConstants.CONTROLLER_LOG_PREFIX, productId);
        try {
            ProductDetailsDTO product = productDetailsService.getProductByProductId(productId);
            return ResponseEntity.ok(product);
        } catch (IllegalArgumentException e) {
            log.warn("{}Invalid product ID: {}", AppConstants.CONTROLLER_LOG_PREFIX, e.getMessage(), e);
            return ResponseEntity.badRequest().body(generateErrorResponse("Invalid product ID: " + e.getMessage()));
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().startsWith(AppConstants.PRODUCT_NOT_FOUND)) {
                log.warn("{}Product not found: {}", AppConstants.CONTROLLER_LOG_PREFIX, e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(generateErrorResponse(e.getMessage()));
            } else {
                log.error("{}Unexpected error fetching product ID {}: {}", AppConstants.CONTROLLER_LOG_PREFIX, productId, e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(generateErrorResponse(AppConstants.FAILED + ": " + e.getMessage()));
            }
        }
    }

    //======================================get All Product Details===============================================================================
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Get All Products", description = "Fetch a list of all products")
    @GetMapping
    public ResponseEntity<?> getAllProductDetails() {
        log.info(AppConstants.CONTROLLER_LOG_PREFIX + "Fetching all products");
        try {
            List<ProductDetailsDTO> products = productDetailsService.getAllProducts();
            return new ResponseEntity<>(products, HttpStatus.OK);
        } catch (RuntimeException e) {
            log.error("Runtime error fetching all products: {}", e.getMessage(), e);
            return new ResponseEntity<>(generateErrorResponse(AppConstants.FAILED + ": " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // =============================== Get Top Trending Products ====================================
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Get Top Trending Products", description = "Fetch a list of top trending products, sorted by admin-defined order")
    @GetMapping("/trending")
    public ResponseEntity<?> getTopTrendingProducts() {
        log.info(AppConstants.CONTROLLER_LOG_PREFIX + "Fetching top trending products.");
        try {
            List<ProductDetailsDTO> trendingProducts = productDetailsService.getTopTrendingProducts();
            return new ResponseEntity<>(trendingProducts, HttpStatus.OK);
        } catch (RuntimeException e) {
            log.error("Runtime error fetching top trending products: {}", e.getMessage(), e);
            return new ResponseEntity<>(generateErrorResponse(AppConstants.FAILED + ": " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // =============================== Get Top Selling Products ====================================
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Get Top Selling Products", description = "Fetch a list of top selling products, sorted by admin-defined order")
    @GetMapping("/selling")
    public ResponseEntity<?> getTopSellingProducts() {
        log.info(AppConstants.CONTROLLER_LOG_PREFIX + "Fetching top selling products.");
        try {
            List<ProductDetailsDTO> sellingProducts = productDetailsService.getTopSellingProducts();
            return new ResponseEntity<>(sellingProducts, HttpStatus.OK);
        } catch (RuntimeException e) {
            log.error("Runtime error fetching top selling products: {}", e.getMessage(), e);
            return new ResponseEntity<>(generateErrorResponse(AppConstants.FAILED + ": " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // =============================== Get Products by Category ID ====================================
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @Operation(summary = "Get Products by Category ID", description = "Fetch products belonging to a specific category")
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getProductsByCategory(@PathVariable Long categoryId)
    {
        log.info("{}Fetching products by category ID: {}", AppConstants.CONTROLLER_LOG_PREFIX, categoryId);
        try {
            List<ProductDetailsDTO> products = productDetailsService.getProductsByCategory(categoryId);
            return new ResponseEntity<>(products, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.warn("{}Invalid category ID: {}", AppConstants.CONTROLLER_LOG_PREFIX, e.getMessage(), e);
            return ResponseEntity.badRequest().body(generateErrorResponse("Invalid category ID: " + e.getMessage()));
        } catch (RuntimeException e) {
            log.error("Runtime error fetching products by category ID {}: {}", categoryId, e.getMessage(), e);
            return new ResponseEntity<>(generateErrorResponse(AppConstants.FAILED + ": " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //============================== NEW: Endpoint to update Trending Product Order ===================================
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update Trending Product Order", description = "Allows admin to set the display order for top trending products")
    @PutMapping("/update-trending-order")
    public ResponseEntity<Void> updateTrendingProductOrder(@RequestBody List<ProductOrderUpdateDTO> updates) 
    {
        log.info(AppConstants.CONTROLLER_LOG_PREFIX + "Received request to update trending product orders. Count: {}", updates.size());
        productDetailsService.updateTrendingProductDisplayOrders(updates);
        return ResponseEntity.ok().build();
    }

    //============================== NEW: Endpoint to update Selling Product Order ===================================
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update Selling Product Order", description = "Allows admin to set the display order for top selling products")
    @PutMapping("/update-selling-order")
    public ResponseEntity<Void> updateSellingProductOrder(@RequestBody List<ProductOrderUpdateDTO> updates)
    {
        log.info(AppConstants.CONTROLLER_LOG_PREFIX + "Received request to update selling product orders. Count: {}", updates.size());
        productDetailsService.updateSellingProductDisplayOrders(updates);
        return ResponseEntity.ok().build();
    }


    //===========================================delete Product By ProductId================================================================


    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete Product by ID", description = "Delete a product by its unique ID (soft delete)")
    @DeleteMapping("/{productId}")
    public ResponseEntity<?> deleteProductByProductId(@PathVariable Long productId) {
        log.info(AppConstants.CONTROLLER_LOG_PREFIX + "Deleting product by ID: {}", productId);
        try {
            productDetailsService.deleteProductByProductId(productId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            if (e.getMessage().startsWith(AppConstants.PRODUCT_NOT_FOUND)) {
                return new ResponseEntity<>(generateErrorResponse(e.getMessage()), HttpStatus.NOT_FOUND);
            } else {
                log.error("Error deleting product by ID: {}", productId, e);
                return new ResponseEntity<>(generateErrorResponse(AppConstants.FAILED + ": " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    private String generateErrorResponse(String message) 
    {
        return String.format("{\"error\": \"%s\"}", message);
    }
}