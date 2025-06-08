package com.application.service;

import com.application.dto.ProductDetailsDTO;
import com.application.dto.ProductOrderUpdateDTO; // Import the new DTO

import java.util.List;

public interface ProductDetailsService {
    ProductDetailsDTO createProduct(ProductDetailsDTO productDTO);
    ProductDetailsDTO getProductByProductId(Long productId);
    List<ProductDetailsDTO> getAllProducts();
    ProductDetailsDTO updateProductByProductId(Long productId, ProductDetailsDTO productDTO);
    void deleteProductByProductId(Long productId);
    List<ProductDetailsDTO> getTopTrendingProducts();
    List<ProductDetailsDTO> getTopSellingProducts();
    List<ProductDetailsDTO> getProductsByCategory(Long categoryId);

    // New methods for updating product orders
    void updateTrendingProductDisplayOrders(List<ProductOrderUpdateDTO> updates);
    void updateSellingProductDisplayOrders(List<ProductOrderUpdateDTO> updates);
}