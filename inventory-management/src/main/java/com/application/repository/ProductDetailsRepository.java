package com.application.repository;

import com.application.entity.ProductDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;


public interface ProductDetailsRepository extends JpaRepository<ProductDetails, Long> {

    // Find active product by name (used in create/update to avoid duplicates)
    Optional<ProductDetails> findByProductNameAndIsActiveTrue(String productName);

    // Find all active products by category
    List<ProductDetails> findByProductCategory_ProductCategoryIdAndIsActiveTrue(Long categoryId);

    // Find all active products (to be used in getAllProducts)
    List<ProductDetails> findByIsActiveTrue();

    // Find active product by ID (to avoid fetching soft-deleted products)
    Optional<ProductDetails> findByProductDetailsIdAndIsActiveTrue(Long productId);

    // Changed: Find all active products that have a trending order, sorted by that order
    // Ensure isActiveTrue is also considered.
    List<ProductDetails> findByTrendingDisplayOrderIsNotNullAndIsActiveTrueOrderByTrendingDisplayOrderAsc();

    // Changed: Find all active products that have a selling order, sorted by that order
    // Ensure isActiveTrue is also considered.
    List<ProductDetails> findBySellingDisplayOrderIsNotNullAndIsActiveTrueOrderBySellingDisplayOrderAsc();

    // New: Find products that currently have a trending order (to null them out)
    @Query("SELECT p FROM ProductDetails p WHERE p.trendingDisplayOrder IS NOT NULL")
    List<ProductDetails> findByTrendingDisplayOrderIsNotNull();

    // New: Find products that currently have a selling order (to null them out)
    @Query("SELECT p FROM ProductDetails p WHERE p.sellingDisplayOrder IS NOT NULL")
    List<ProductDetails> findBySellingDisplayOrderIsNotNull();

    // If you need to update order of multiple products efficiently
    // This is optional for now, as we'll do it product by product in service,
    // but useful for batch updates.
    @Modifying
    @Query("UPDATE ProductDetails p SET p.trendingDisplayOrder = :order WHERE p.productDetailsId = :id")
    void updateTrendingDisplayOrder(@Param("id") Long id, @Param("order") Integer order);

    @Modifying
    @Query("UPDATE ProductDetails p SET p.sellingDisplayOrder = :order WHERE p.productDetailsId = :id")
    void updateSellingDisplayOrder(@Param("id") Long id, @Param("order") Integer order);
}