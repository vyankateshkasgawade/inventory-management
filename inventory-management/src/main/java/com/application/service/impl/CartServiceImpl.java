package com.application.service.impl;

import com.application.dto.CartDTO;
import com.application.entity.AppUser;
import com.application.entity.Cart;
import com.application.entity.ProductDetails;
import com.application.exception.CartNotFoundException;
import com.application.exception.ResourceNotFoundException;
import com.application.repository.AppUserRepository;
import com.application.repository.CartRepository;
import com.application.repository.ProductDetailsRepository;
import com.application.service.CartService;
import com.application.util.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import Transactional

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final AppUserRepository userRepository;
    private final ProductDetailsRepository productRepository;

    //=================================add To Cart=============================================================
    @Override
    @Transactional // Added @Transactional for atomicity
    public CartDTO addToCart(CartDTO cartDTO) {
        log.info(AppConstants.SERVICE_LOG_PREFIX + "Adding product {} to cart for user {}", cartDTO.getProductId(), cartDTO.getUserId());

        try {
            AppUser user = userRepository.findById(cartDTO.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException(AppConstants.USER_NOT_FOUND + cartDTO.getUserId()));

            ProductDetails product = productRepository.findById(cartDTO.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(AppConstants.PRODUCT_NOT_FOUND + cartDTO.getProductId()));

            // <<< START OF ORIGINAL CODE - NO CHANGES HERE >>>
            // Check if the product already exists in the user's active cart
            Optional<Cart> existingCartItemOptional = cartRepository.findByUserUserIdAndProductProductDetailsIdAndIsActiveTrue(cartDTO.getUserId(), cartDTO.getProductId());

            Cart cartItem;
            if (existingCartItemOptional.isPresent()) {
                // If the item exists, update its quantity and total amount
                cartItem = existingCartItemOptional.get();
                cartItem.setQuantity(cartItem.getQuantity() + cartDTO.getQuantity()); // Increment quantity
                log.info("Product already in cart. Increasing quantity for product ID: {}", product.getProductDetailsId());
            } else {
                // If the item does not exist, create a new cart entry
                cartItem = new Cart();
                cartItem.setUser(user);
                cartItem.setProduct(product);
                cartItem.setQuantity(cartDTO.getQuantity());
                cartItem.setAddedDate(new Date());
                cartItem.setIsActive(true); // Ensure it's marked as active
                log.info("New product added to cart. Product ID: {}", product.getProductDetailsId());
            }

            // Always update product-related details from the current product, in case they changed
            cartItem.setProductName(product.getProductName()); // Set product name from product entity
            cartItem.setImageUrl(product.getImageUrl()); // Set image URL from product entity

            // Recalculate final total amount based on the *updated* quantity
            BigDecimal finalTotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            cartItem.setFinalTotalAmount(finalTotal);

            return mapToDTO(cartRepository.save(cartItem));
            // <<< END OF ORIGINAL CODE - NO CHANGES HERE >>>

        } catch (ResourceNotFoundException e) {
            log.error("Resource not found: {}", e.getMessage(), e);
            throw e;
        } catch (RuntimeException e) {
            log.warn("Runtime exception during cart operation: {}", e.getMessage(), e);
            throw e;
        }
    }

    //==================================update Cart By Cart Id============================================================
    @Override
    public CartDTO updateCartByCartId(Long cartId, CartDTO cartDTO) {
        log.info(AppConstants.SERVICE_LOG_PREFIX + "Updating cart item with ID: {}", cartId);
        try {
            Cart existingCartItem = cartRepository.findById(cartId)
                    .orElseThrow(() -> new ResourceNotFoundException(AppConstants.CART_NOT_FOUND + cartId));

            if (cartDTO.getQuantity() != null) {
                existingCartItem.setQuantity(cartDTO.getQuantity());
                BigDecimal finalTotal = existingCartItem.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(cartDTO.getQuantity()));
                existingCartItem.setFinalTotalAmount(finalTotal);
            }
            // Optionally update imageUrl if it's sent in update DTO
            if (cartDTO.getImageUrl() != null) {
                existingCartItem.setImageUrl(cartDTO.getImageUrl());
            }

            return mapToDTO(cartRepository.save(existingCartItem));
        } catch (ResourceNotFoundException e) {
            log.error("Resource not found: {}", e.getMessage(), e);
            throw e;
        } catch (RuntimeException e) {
            log.warn("Runtime exception during cart update: {}", e.getMessage(), e);
            throw e;
        }
    }

    //====================================get Cart By Cart Id==========================================================
    @Override
    public CartDTO getCartByCartId(Long cartId) {
        log.info(AppConstants.SERVICE_LOG_PREFIX + "Fetching cart item by ID: {}", cartId);
        try {
            Cart cartItem = cartRepository.findById(cartId)
                    .orElseThrow(() -> new CartNotFoundException(AppConstants.CART_NOT_FOUND + cartId));

            if (!cartItem.getIsActive()) {
                throw new CartNotFoundException(AppConstants.CART_NOT_FOUND + cartId);
            }

            return mapToDTO(cartItem);
        } catch (CartNotFoundException e) {
            log.warn("Cart not found: {}", e.getMessage(), e);
            throw e;
        } catch (RuntimeException e) {
            log.warn("Runtime exception during cart retrieval: {}", e.getMessage(), e);
            throw e;
        }
    }

    //=======================================get Carts By User Id=======================================================
    @Override
    public List<CartDTO> getCartsByUserId(Long userId) {
        log.info(AppConstants.SERVICE_LOG_PREFIX + "Fetching cart items for user ID: {}", userId);
        try {
            // This method already filters for active items in the stream
            return cartRepository.findByUserUserId(userId).stream()
                    .filter(Cart::getIsActive) // Only return active cart items
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        } catch (RuntimeException e) {
            log.warn("Runtime exception during cart list fetch: {}", e.getMessage(), e);
            throw e;
        }
    }

    //========================================delete Cart By Cart Id======================================================
    @Override
    public void deleteCartByCartId(Long cartId) {
        log.info(AppConstants.SERVICE_LOG_PREFIX + "Deleting cart item by ID: {}", cartId);
        try {
            Cart cartItem = cartRepository.findById(cartId)
                    .orElseThrow(() -> new CartNotFoundException(AppConstants.CART_NOT_FOUND + cartId));

            // No need to check isActive - just proceed with soft delete
            cartItem.setIsActive(false);
            cartRepository.save(cartItem);
        } catch (CartNotFoundException e) {
            log.warn("Cart not found error: {}", e.getMessage(), e);
            throw e;
        } catch (RuntimeException e) {
            log.warn("Runtime exception during cart deletion: {}", e.getMessage(), e);
            throw e;
        }
    }

    // ====================================================================================================
    // NEW PAYMENT INTEGRATION LOGIC START (for clearing cart after successful payment)

    @Override
    @Transactional
    public void clearCartByUserId(Long userId) {
        log.info(AppConstants.SERVICE_LOG_PREFIX + "Clearing cart for user ID: {}", userId);
        try {
            // Find the user to ensure existence
            AppUser user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException(AppConstants.USER_NOT_FOUND + userId));

            // Get all active cart items for the user
            // Reusing findByUserUserId and filtering, which is consistent with getCartsByUserId
            List<Cart> activeCartItems = cartRepository.findByUserUserId(userId)
                    .stream()
                    .filter(Cart::getIsActive)
                    .collect(Collectors.toList());

            if (activeCartItems.isEmpty()) {
                log.info("Cart already empty for user {}. No items to clear.", userId);
                return; // Nothing to do if cart is already empty
            }

            // Iterate and soft-delete each active item
            for (Cart item : activeCartItems) {
                // Reusing the existing soft-delete logic
                this.deleteCartByCartId(item.getCartId());
            }
            log.info("Cart cleared successfully for user {}.", userId);
        } catch (ResourceNotFoundException e) {
            log.error("Resource not found when clearing cart for user {}: {}", userId, e.getMessage(), e);
            throw e; // Re-throw to be handled by controller's exception handler
        } catch (RuntimeException e) {
            log.error("Error clearing cart for user {}: {}", userId, e.getMessage(), e);
            throw e; // Re-throw any other runtime exception
        }
    }

    private CartDTO mapToDTO(Cart entity) {
        return CartDTO.builder()
                .cartId(entity.getCartId())
                .userId(entity.getUser().getUserId())
                .userName(entity.getUser().getName())
                .productId(entity.getProduct().getProductDetailsId())
                .productName(entity.getProduct().getProductName())
                .quantity(entity.getQuantity())
                .addedDate(entity.getAddedDate())
                .finalTotalAmount(entity.getFinalTotalAmount())
                .isActive(entity.getIsActive())
                .imageUrl(entity.getImageUrl())
                .price(entity.getProduct().getPrice())
                .build();
    }
}