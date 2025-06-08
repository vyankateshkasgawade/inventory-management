package com.application.controller;

import com.application.entity.Wishlist;
import com.application.service.impl.WishlistService;
import com.application.dto.WishlistDTO; // Import the DTO

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Add this import
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

// This is crucial: Mark it as a REST Controller and define its base path
@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    @Autowired
    private WishlistService wishlistService;

    // Endpoint to get a user's wishlist
    // Maps to frontend: :8080/api/wishlist/user/{userId}
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')") // Add security annotation
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Wishlist>> getWishlistByUserId(@PathVariable Long userId) {
        List<Wishlist> wishlist = wishlistService.getWishlistByUserId(userId);
        return ResponseEntity.ok(wishlist);
    }

    /**
     * Endpoint to add a product to the wishlist.
     * Accepts a WishlistDTO as a request body.
     * Maps to frontend: POST to :8080/api/wishlist with JSON body { userId: 1, productId: 1 }
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')") // Add security annotation
    @PostMapping
    public ResponseEntity<Wishlist> addProductToWishlist(@RequestBody WishlistDTO wishlistDto) {
        try {
            Wishlist newWishlistEntry = wishlistService.addProductToWishlist(
                wishlistDto.getUserId(),
                wishlistDto.getProductId()
            );
            return new ResponseEntity<>(newWishlistEntry, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Return a more specific error message in the body
            // Consider returning a custom error DTO or a string for better client-side handling
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }

    // Endpoint to remove a product from the wishlist
    // Maps to frontend: DELETE to :8080/api/wishlist/{wishlistId}
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')") // Add security annotation
    @DeleteMapping("/{wishlistId}")
    public ResponseEntity<Void> removeProductFromWishlist(@PathVariable Long wishlistId) {
        wishlistService.removeProductFromWishlist(wishlistId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    // Endpoint to check if a product is in a user's wishlist
    // Maps to frontend: :8080/api/wishlist/check/{userId}/{productId}
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')") // Add security annotation
    @GetMapping("/check/{userId}/{productId}")
    public ResponseEntity<Boolean> isProductInWishlist(@PathVariable Long userId, @PathVariable Long productId) {
        boolean isInWishlist = wishlistService.isProductInWishlist(userId, productId);
        return ResponseEntity.ok(isInWishlist);
    }

    // Endpoint to get wishlistId for a specific product and user
    // Maps to frontend: :8080/api/wishlist/id/{userId}/{productId}
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')") // Add security annotation
    @GetMapping("/id/{userId}/{productId}")
    public ResponseEntity<Long> getWishlistIdForProduct(@PathVariable Long userId, @PathVariable Long productId) {
        Optional<Long> wishlistId = wishlistService.getWishlistIdForProduct(userId, productId);
        return wishlistId.map(ResponseEntity::ok)
                         .orElseGet(() -> ResponseEntity.notFound().build()); // 404 if not found
    }
}