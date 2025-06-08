package com.application.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.application.dto.ProductDetailsDTO;
import com.application.dto.ProductOrderUpdateDTO; // Import the new DTO
import com.application.entity.ProductCategory;
import com.application.entity.ProductDetails;
import com.application.exception.ResourceNotFoundException;
import com.application.repository.ProductCategoryRepository;
import com.application.repository.ProductDetailsRepository;
import com.application.service.ProductDetailsService;
import com.application.util.AppConstants;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductDetailsServiceImpl implements ProductDetailsService {

    private final ProductDetailsRepository productRepository;
    private final ProductCategoryRepository categoryRepository;

    //================================================= create Product ========================================================================
    @Override
    @Transactional
    public ProductDetailsDTO createProduct(ProductDetailsDTO productDTO) {
        log.info(AppConstants.SERVICE_LOG_PREFIX + "Creating new product: {}", productDTO.getProductName());

        try {
            Optional<ProductDetails> existingProductByName = productRepository.findByProductNameAndIsActiveTrue(productDTO.getProductName());
            if (existingProductByName.isPresent()) {
                throw new RuntimeException("Product with name '" + productDTO.getProductName() + "' already exists.");
            }

            ProductCategory category = categoryRepository.findById(productDTO.getProductCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(AppConstants.CATEGORY_NOT_FOUND + productDTO.getProductCategoryId()));

            ProductDetails product = mapToEntity(productDTO);
            product.setProductCategory(category);
            product.setIsActive(true);
            // Explicitly set display orders to null when creating a new product
            // They are managed by separate sequencing operations.
            product.setTrendingDisplayOrder(null);
            product.setSellingDisplayOrder(null);

            return mapToDTO(productRepository.save(product));

        } catch (DataIntegrityViolationException e) {
            if (e.getMessage().contains("unique constraint") && e.getMessage().contains("product_name")) {
                throw new RuntimeException("Product with name '" + productDTO.getProductName() + "' already exists.");
            } else {
                throw new RuntimeException("Error saving product: " + e.getMessage(), e);
            }
        } catch (ResourceNotFoundException e) {
            log.error("Resource not found: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            throw e;
        }
    }

    //================================================= get Product By ProductId ========================================================================
    @Override
    public ProductDetailsDTO getProductByProductId(Long productId) {
        log.info(AppConstants.SERVICE_LOG_PREFIX + "Fetching product by ID: {}", productId);
        try {
            ProductDetails product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException(AppConstants.PRODUCT_NOT_FOUND + productId));
            if(!product.getIsActive()) {
                throw new ResourceNotFoundException(AppConstants.PRODUCT_NOT_FOUND + productId);
            }
            return mapToDTO(product);
        } catch (ResourceNotFoundException e) {
            log.error("Resource not found: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            throw e;
        }
    }

    //================================================= get All Products ========================================================================
    @Override
    public List<ProductDetailsDTO> getAllProducts() {
        log.info(AppConstants.SERVICE_LOG_PREFIX + "Fetching all products");
        try {
            return productRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
        } catch (RuntimeException e) {
            throw e;
        }
    }

    //================================================= update Product By ProductId ========================================================================
    @Override
    @Transactional
    public ProductDetailsDTO updateProductByProductId(Long productId, ProductDetailsDTO productDTO) {
        log.info(AppConstants.SERVICE_LOG_PREFIX + "Updating product with ID: {}", productId);

        try {
            ProductDetails existingProduct = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException(AppConstants.PRODUCT_NOT_FOUND + productId));

            Optional<ProductDetails> existingProductByName = productRepository.findByProductNameAndIsActiveTrue(productDTO.getProductName());
            if (existingProductByName.isPresent() && !existingProductByName.get().getProductDetailsId().equals(productId)) {
                throw new RuntimeException("Product with name '" + productDTO.getProductName() + "' already exists.");
            }

            ProductCategory category = categoryRepository.findById(productDTO.getProductCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(AppConstants.CATEGORY_NOT_FOUND + productDTO.getProductCategoryId()));

            existingProduct.setProductName(productDTO.getProductName());
            existingProduct.setProductQuantity(productDTO.getProductQuantity());
            existingProduct.setPrice(productDTO.getPrice());
            existingProduct.setProductCategory(category);
            existingProduct.setIsActive(productDTO.getIsActive());
            existingProduct.setImageUrl(productDTO.getImageUrl());

            // Do NOT update trendingDisplayOrder/sellingDisplayOrder from the general product update DTO.
            // These are managed exclusively by the dedicated sequence update endpoints.
            // Existing values on `existingProduct` entity should be retained if not touched by sequencing.
            // We explicitly don't set them from `productDTO` here.

            log.info("Product status updated to: {}", productDTO.getIsActive() ? "Active" : "Inactive");

            return mapToDTO(productRepository.save(existingProduct));

        } catch (ResourceNotFoundException e) {
            log.error("Resource not found: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            throw e;
        }
    }

    //================================================= delete Product By ProductId ========================================================================
    @Override
    @Transactional
    public void deleteProductByProductId(Long productId) {
        log.info(AppConstants.SERVICE_LOG_PREFIX + "Deleting product by ID: {}", productId);
        try {
            ProductDetails product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException(AppConstants.PRODUCT_NOT_FOUND + productId));
            product.setIsActive(false);
            // When soft-deleting, also remove from trending/selling lists
            product.setTrendingDisplayOrder(null);
            product.setSellingDisplayOrder(null);
            productRepository.save(product);
        } catch (RuntimeException e) {
            throw e;
        }
    }

    //================================================= get Top Trending Products ========================================================================
    @Override
    public List<ProductDetailsDTO> getTopTrendingProducts() {
        log.info(AppConstants.SERVICE_LOG_PREFIX + "Fetching all top trending products");
        try {
            // Use the new repository method that sorts by trendingDisplayOrder
            return productRepository.findByTrendingDisplayOrderIsNotNullAndIsActiveTrueOrderByTrendingDisplayOrderAsc().stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        } catch (RuntimeException e) {
            log.error("Error fetching top trending products: {}", e.getMessage(), e);
            throw e;
        }
    }

    //================================================= get Top Selling Products ========================================================================
    @Override
    public List<ProductDetailsDTO> getTopSellingProducts() {
        log.info(AppConstants.SERVICE_LOG_PREFIX + "Fetching all top selling products");
        try {
            // Use the new repository method that sorts by sellingDisplayOrder
            return productRepository.findBySellingDisplayOrderIsNotNullAndIsActiveTrueOrderBySellingDisplayOrderAsc().stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        } catch (RuntimeException e) {
            log.error("Error fetching top selling products: {}", e.getMessage(), e);
            throw e;
        }
    }

    //================================================= get Products by Category ID ========================================================================
    @Override
    public List<ProductDetailsDTO> getProductsByCategory(Long categoryId) {
        log.info(AppConstants.SERVICE_LOG_PREFIX + "Fetching products by category ID: {}", categoryId);
        try {
            return productRepository.findByProductCategory_ProductCategoryIdAndIsActiveTrue(categoryId).stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        } catch (RuntimeException e) {
            log.error("Error fetching products by category ID {}: {}", categoryId, e.getMessage(), e);
            throw e;
        }
    }

    //============================== NEW: Update Trending Product Display Orders ===================================
    @Override
    @Transactional
    public void updateTrendingProductDisplayOrders(List<ProductOrderUpdateDTO> updates) {
        log.info(AppConstants.SERVICE_LOG_PREFIX + "Updating trending product display orders. Count: {}", updates.size());

        // Create a set of product IDs present in the incoming updates
        List<Long> updatedProductIds = updates.stream()
                                            .map(ProductOrderUpdateDTO::getId)
                                            .collect(Collectors.toList());

        // Nullify trending_display_order for all products that were previously trending
        // but are NOT in the current update list (i.e., they were removed from the trending list)
        List<ProductDetails> productsToClearOrder = productRepository.findByTrendingDisplayOrderIsNotNull();
        for (ProductDetails product : productsToClearOrder) {
            if (!updatedProductIds.contains(product.getProductDetailsId())) {
                product.setTrendingDisplayOrder(null);
                productRepository.save(product); // Save individually or batch save later
            }
        }

        // Apply new orders for products in the updates list
        for (ProductOrderUpdateDTO update : updates) {
            productRepository.findById(update.getId()).ifPresent(product -> {
                product.setTrendingDisplayOrder(update.getDisplayOrder());
                productRepository.save(product); // Save individually
            });
        }
    }

    //============================== NEW: Update Selling Product Display Orders ===================================
    @Override
    @Transactional
    public void updateSellingProductDisplayOrders(List<ProductOrderUpdateDTO> updates) {
        log.info(AppConstants.SERVICE_LOG_PREFIX + "Updating selling product display orders. Count: {}", updates.size());

        // Create a set of product IDs present in the incoming updates
        List<Long> updatedProductIds = updates.stream()
                                            .map(ProductOrderUpdateDTO::getId)
                                            .collect(Collectors.toList());

        // Nullify selling_display_order for all products that were previously selling
        // but are NOT in the current update list
        List<ProductDetails> productsToClearOrder = productRepository.findBySellingDisplayOrderIsNotNull();
        for (ProductDetails product : productsToClearOrder) {
            if (!updatedProductIds.contains(product.getProductDetailsId())) {
                product.setSellingDisplayOrder(null);
                productRepository.save(product); // Save individually or batch save later
            }
        }

        // Apply new orders for products in the updates list
        for (ProductOrderUpdateDTO update : updates) {
            productRepository.findById(update.getId()).ifPresent(product -> {
                product.setSellingDisplayOrder(update.getDisplayOrder());
                productRepository.save(product); // Save individually
            });
        }
    }

    //============================================================================================================================================
    private ProductDetails mapToEntity(ProductDetailsDTO dto) {
        // Use the new builder methods for trendingDisplayOrder and sellingDisplayOrder
        // but set them to null as they are managed by separate endpoints
        return ProductDetails.builder()
                .productDetailsId(dto.getProductDetailsId())
                .productName(dto.getProductName())
                .productQuantity(dto.getProductQuantity())
                .price(dto.getPrice())
                .imageUrl(dto.getImageUrl())
                // Do NOT map trendingDisplayOrder/sellingDisplayOrder from DTO here
                // They are set to null initially and managed by dedicated admin functions.
                .trendingDisplayOrder(null) // Explicitly set to null for new product creation/general updates
                .sellingDisplayOrder(null)  // Explicitly set to null for new product creation/general updates
                .build();
    }

    private ProductDetailsDTO mapToDTO(ProductDetails entity) {
        // Use the new builder methods for trendingDisplayOrder and sellingDisplayOrder
        return ProductDetailsDTO.builder()
                .productDetailsId(entity.getProductDetailsId())
                .productName(entity.getProductName())
                .productQuantity(entity.getProductQuantity())
                .price(entity.getPrice())
                .productCategoryId(entity.getProductCategory().getProductCategoryId())
                .productCategoryName(entity.getProductCategoryName())
                .isActive(entity.getIsActive())
                .imageUrl(entity.getImageUrl())
                // Map the new fields from entity to DTO
                .trendingDisplayOrder(entity.getTrendingDisplayOrder())
                .sellingDisplayOrder(entity.getSellingDisplayOrder())
                .build();
    }
}