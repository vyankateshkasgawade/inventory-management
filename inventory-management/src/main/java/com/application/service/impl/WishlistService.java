package com.application.service.impl;
import com.application.entity.Wishlist;
import com.application.exception.ResourceNotFoundException;
import com.application.repository.WishlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    // Method to add a product to the wishlist
    public Wishlist addProductToWishlist(Long userId, Long productId) {
        // Check if the product is already in the wishlist for this user
        if (wishlistRepository.existsByUserIdAndProductId(userId, productId)) {
            // You can throw a custom exception or just return null/the existing entry
            throw new IllegalArgumentException("Product already in wishlist for user " + userId);
        }

        Wishlist wishlist = new Wishlist();
        wishlist.setUserId(userId);
        wishlist.setProductId(productId);
        // Set addedDate if you have it in your entity
        return wishlistRepository.save(wishlist);
    }

    // Method to get all wishlist items for a user
    public List<Wishlist> getWishlistByUserId(Long userId) {
        return wishlistRepository.findByUserId(userId);
    }

    // Method to remove a product from the wishlist by wishlistId
    @Transactional // Required for delete operations that modify the database
    public void removeProductFromWishlist(Long wishlistId) {
        if (!wishlistRepository.existsById(wishlistId)) {
            throw new ResourceNotFoundException("Wishlist entry not found with ID: " + wishlistId);
        }
        wishlistRepository.deleteById(wishlistId);
    }

    // Optional: Method to check if a product is in a user's wishlist
    public boolean isProductInWishlist(Long userId, Long productId) {
        return wishlistRepository.existsByUserIdAndProductId(userId, productId);
    }

    // Optional: Get wishlistId for a product and user (useful for frontend removal)
    public Optional<Long> getWishlistIdForProduct(Long userId, Long productId) {
        return wishlistRepository.findByUserIdAndProductId(userId, productId)
                .map(Wishlist::getWishlistId);
    }
}
