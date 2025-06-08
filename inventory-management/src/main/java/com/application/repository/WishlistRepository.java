package com.application.repository;
import com.application.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    // Find all wishlist entries for a specific user
    List<Wishlist> findByUserId(Long userId);

    // Find a specific wishlist entry by user ID and product ID
    Optional<Wishlist> findByUserIdAndProductId(Long userId, Long productId);

    // Check if a product is in a user's wishlist
    boolean existsByUserIdAndProductId(Long userId, Long productId);

    // Delete a wishlist entry by its ID
    void deleteByWishlistId(Long wishlistId); // You might need @Transactional for this
}