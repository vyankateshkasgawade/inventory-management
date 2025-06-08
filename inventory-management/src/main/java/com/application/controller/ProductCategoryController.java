package com.application.controller;

import com.application.dto.ProductCategoryDTO;
import com.application.exception.ProductCategoryException;
import com.application.exception.ResourceNotFoundException;
import com.application.service.ProductCategoryService;
import com.application.util.AppConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
//@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "ProductCategoryController", description = "Operations related to Product Category management")
public class ProductCategoryController {

    private final ProductCategoryService productCategoryService;
    
    
 //==================================================create New Category=================================================================================
    
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new Product Category", description = "Create a new product category in the system")
    @PostMapping
    public ResponseEntity<?> createNewCategory(@RequestBody ProductCategoryDTO categoryDTO) {
        log.info(AppConstants.CONTROLLER_LOG_PREFIX + "Creating new product category: {}", categoryDTO.getProductCategoryName());
        try {
            ProductCategoryDTO createdCategory = productCategoryService.createCategory(categoryDTO);
            return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid category input: {}", e.getMessage(), e);
            return new ResponseEntity<>("Invalid input: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (DataIntegrityViolationException e) {
            log.warn("DB constraint violation while creating category: {}", e.getMessage(), e);
            return new ResponseEntity<>("Conflict: DB constraint violation", HttpStatus.CONFLICT);
        } catch (ProductCategoryException e) {
            log.error("ProductCategoryException: {}", e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        } catch (Exception e) {
            log.error(AppConstants.CONTROLLER_LOG_PREFIX + "Unexpected error creating product category: {}", categoryDTO.getProductCategoryName(), e);
            return new ResponseEntity<>(AppConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    
//===================================================get Category By Category Id====================================================
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get Product Category by ID", description = "Fetch a product category by its unique ID")
    @GetMapping("/{categoryId}")
    public ResponseEntity<?> getCategoryByCategoryId(@PathVariable Long categoryId) {
        log.info(AppConstants.CONTROLLER_LOG_PREFIX + "Fetching product category with ID: {}", categoryId);
        try {
            ProductCategoryDTO category = productCategoryService.getCategoryByCategoryId(categoryId);
            return new ResponseEntity<>(category, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            log.error("Category not found for ID: {}", categoryId, e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid category ID fetch request: {}", e.getMessage(), e);
            return new ResponseEntity<>("Invalid category ID", HttpStatus.BAD_REQUEST);
        } catch (ProductCategoryException e) {
            log.error("ProductCategoryException: {}", e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Unexpected error fetching category ID {}: {}", categoryId, e.getMessage(), e);
            return new ResponseEntity<>(AppConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

   //=========================================get All Product Categories=========================================================
    
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    
    @Operation(summary = "Get all Product Categories", description = "Fetch a list of all product categories")
    @GetMapping
    public ResponseEntity<?> getAllProductCategories() {
        log.info(AppConstants.CONTROLLER_LOG_PREFIX + "Fetching all product categories");
        try {
            List<ProductCategoryDTO> categories = productCategoryService.getAllCategories();
            return new ResponseEntity<>(categories, HttpStatus.OK);
        } catch (ProductCategoryException e) {
            log.error("Error fetching all product categories: {}", e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Unexpected error fetching all product categories: {}", e.getMessage(), e);
            return new ResponseEntity<>(AppConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//==============================================update Category By Category Id========================================================
    
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update Product Category by ID", description = "Update an existing product category based on its ID")
    @PutMapping("/{categoryId}")
    public ResponseEntity<?> updateCategoryByCategoryId(@PathVariable Long categoryId, @RequestBody ProductCategoryDTO categoryDTO) {
        log.info(AppConstants.CONTROLLER_LOG_PREFIX + "Updating product category with ID {}: {}", categoryId, categoryDTO);
        try {
            ProductCategoryDTO updatedCategory = productCategoryService.updateCategoryByCategoryId(categoryId, categoryDTO);
            return new ResponseEntity<>(updatedCategory, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid category update input: {}", e.getMessage(), e);
            return new ResponseEntity<>("Invalid input: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (ResourceNotFoundException e) {
            log.error("Category not found for update: {}", categoryId, e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (ProductCategoryException e) {
            log.error("ProductCategoryException: {}", e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Unexpected error updating category ID {}: {}", categoryId, e.getMessage(), e);
            return new ResponseEntity<>(AppConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    
    
    //================================================delete Category By Category Id=============================================
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete Product Category by ID", description = "Delete a product category by its unique ID")
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<?> deleteCategoryByCategoryId(@PathVariable Long categoryId) {
        log.info(AppConstants.CONTROLLER_LOG_PREFIX + "Deleting product category with ID: {}", categoryId);
        try {
            productCategoryService.deleteCategoryByCategoryId(categoryId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (ResourceNotFoundException e) {
            log.error("Category not found for deletion: {}", categoryId, e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid category ID for delete: {}", e.getMessage(), e);
            return new ResponseEntity<>("Invalid category ID", HttpStatus.BAD_REQUEST);
        } catch (ProductCategoryException e) {
            log.error("ProductCategoryException: {}", e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Unexpected error deleting category ID {}: {}", categoryId, e.getMessage(), e);
            return new ResponseEntity<>(AppConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
