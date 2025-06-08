package com.application.repository;

import com.application.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    List<Cart> findByUserUserId(Long userId);
    //Optional<Cart> findByUserUserIdAndProductProductDetailsId(Long userId, Long productId);
    Optional<Cart> findByUserUserIdAndProductProductDetailsIdAndIsActiveTrue(Long userId, Long productId);


}