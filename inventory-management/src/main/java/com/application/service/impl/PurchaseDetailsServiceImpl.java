package com.application.service.impl;

import com.application.dto.CartDTO;
import com.application.entity.Cart;
import com.application.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.application.dto.PurchaseDetailsDTO;
import com.application.entity.AppUser;
import com.application.entity.ProductDetails;
import com.application.entity.PurchaseDetails;
import com.application.exception.PurchaseAlreadyExistsException;
import com.application.exception.ResourceNotFoundException;
import com.application.exception.UserNotFoundException;
import com.application.repository.AppUserRepository;
import com.application.repository.ProductDetailsRepository;
import com.application.repository.PurchaseDetailsRepository;
import com.application.service.PurchaseDetailsService;
import com.application.util.AppConstants;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseDetailsServiceImpl implements PurchaseDetailsService {

    private final PurchaseDetailsRepository purchaseRepository;
    private final AppUserRepository userRepository;
    private final ProductDetailsRepository productRepository;
    private final CartRepository cartRepository;

    //==============================================create Purchase=========================================================================
    @Override
    @Transactional
    public PurchaseDetailsDTO createPurchase(PurchaseDetailsDTO purchaseDTO) {
        log.info(AppConstants.SERVICE_LOG_PREFIX + "Creating new purchase record for user ID: {}, product ID: {}",
                purchaseDTO.getUserId(), purchaseDTO.getProductId());

        try {
            AppUser user = userRepository.findById(purchaseDTO.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException(AppConstants.USER_NOT_FOUND + purchaseDTO.getUserId()));

            ProductDetails product = productRepository.findById(purchaseDTO.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(AppConstants.PRODUCT_NOT_FOUND + purchaseDTO.getProductId()));

            PurchaseDetails purchase = PurchaseDetails.builder()
                    .user(user) // Keeping the @ManyToOne relationship
                    .product(product) // Keeping the @ManyToOne relationship
                    .productCount(purchaseDTO.getProductCount())
                    .purchaseDate(new Date())
                    .isActive(true)
                    // *** CRITICAL ADDITION: Set the direct column values ***
                    .productNameAtPurchase(product.getProductName()) // Assuming ProductDetails has getProductName()
                    .userNameAtPurchase(user.getName()) // Assuming AppUser has getName()
                    .build();

            return mapToDTO(purchaseRepository.save(purchase));

        } catch (ResourceNotFoundException e) {
            log.error(AppConstants.SERVICE_LOG_PREFIX + "Resource not found while creating purchase: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error(AppConstants.SERVICE_LOG_PREFIX + "Error creating purchase for user ID: {}, product ID: {}", purchaseDTO.getUserId(), purchaseDTO.getProductId(), e);
            throw new RuntimeException(AppConstants.SOMETHING_WENT_WRONG + " " + e.getMessage(), e);
        }
    }

    // =========================================== Create Purchases From Cart =============================================
    @Override
    @Transactional
    public List<PurchaseDetailsDTO> createPurchasesFromCart(Long userId) {
        log.info(AppConstants.SERVICE_LOG_PREFIX + "Creating purchase records from cart for user ID: {}", userId);

        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(AppConstants.USER_NOT_FOUND + userId));

        List<Cart> activeCartItems = cartRepository.findByUserUserId(userId)
                .stream()
                .filter(Cart::getIsActive)
                .collect(Collectors.toList());

        if (activeCartItems.isEmpty()) {
            log.warn("No active cart items found for user ID: {} to convert to purchases.", userId);
            return List.of();
        }

        return activeCartItems.stream()
                .map(cartItem -> {
                    try {
                        ProductDetails product = cartItem.getProduct(); // Get product directly from cartItem's relationship

                        // Handle case where product might somehow be null in cartItem (though it shouldn't be if relationships are good)
                        if (product == null) {
                            log.error("Cart item (ID: {}) for user {} has a null product. Skipping.", cartItem.getCartId(), userId);
                            return null;
                        }

                        PurchaseDetails purchase = PurchaseDetails.builder()
                                .user(user) // Keeping the @ManyToOne relationship
                                .product(product) // Keeping the @ManyToOne relationship
                                .productCount(cartItem.getQuantity())
                                .purchaseDate(new Date())
                                .isActive(true)
                                // *** CRITICAL ADDITION: Set the direct column values ***
                                .productNameAtPurchase(product.getProductName()) // Assuming ProductDetails has getProductName()
                                .userNameAtPurchase(user.getName()) // Assuming AppUser has getName()
                                .build();
                        return mapToDTO(purchaseRepository.save(purchase));
                    } catch (Exception e) {
                        log.error("Failed to convert cart item (ID: {}) to purchase for user {}: {}", cartItem.getCartId(), userId, e.getMessage(), e);
                        throw new RuntimeException("Failed to process cart item into purchase: " + e.getMessage(), e);
                    }
                })
                .filter(java.util.Objects::nonNull) // Filter out any nulls if cart items were skipped
                .collect(Collectors.toList());
    }

    //=============================================get Purchase By Purchase Details Id =========================================================================
    @Override
    public PurchaseDetailsDTO getPurchaseByPurchaseDetailsId(Long purchaseDetailsId) {
        log.info(AppConstants.SERVICE_LOG_PREFIX + "Fetching purchase by ID: {}", purchaseDetailsId);

        try {
            PurchaseDetails purchase = purchaseRepository.findById(purchaseDetailsId)
                    .orElseThrow(() -> new ResourceNotFoundException(AppConstants.PURCHASE_NOT_FOUND + purchaseDetailsId));
            if (!purchase.getIsActive()) {
                throw new ResourceNotFoundException(AppConstants.PURCHASE_NOT_FOUND + purchaseDetailsId);
            }
            return mapToDTO(purchase);

        } catch (IllegalArgumentException e) {
            log.error(AppConstants.SERVICE_LOG_PREFIX + "Invalid purchase details ID: {}", purchaseDetailsId, e);
            throw e;
        } catch (ResourceNotFoundException e) {
            log.warn(AppConstants.SERVICE_LOG_PREFIX + "Purchase not found or inactive for ID: {}", purchaseDetailsId, e);
            throw e;
        } catch (DataIntegrityViolationException e) {
            log.error(AppConstants.SERVICE_LOG_PREFIX + "Data integrity violation while fetching purchase ID: {}", purchaseDetailsId, e);
            throw new RuntimeException("Data integrity error: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            log.error(AppConstants.SERVICE_LOG_PREFIX + "Runtime exception while fetching purchase ID: {}", purchaseDetailsId, e);
            throw e;
        }
    }

    //============================================== get All Purchases =========================================================================
    @Override
    public List<PurchaseDetailsDTO> getAllPurchases() {
        log.info(AppConstants.SERVICE_LOG_PREFIX + "Fetching all purchases");

        try {
            return purchaseRepository.findAll().stream().filter(PurchaseDetails::getIsActive)
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());

        } catch (IllegalArgumentException e) {
            log.error(AppConstants.SERVICE_LOG_PREFIX + "Invalid request to fetch all purchases", e);
            throw e;
        } catch (DataIntegrityViolationException e) {
            log.error(AppConstants.SERVICE_LOG_PREFIX + "Data integrity violation while fetching all purchases", e);
            throw new RuntimeException("Data integrity error: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            log.error(AppConstants.SERVICE_LOG_PREFIX + "Runtime exception while fetching all purchases", e);
            throw e;
        }
    }

    //==============================================get Purchases By User Id=========================================================================
    @Override
    public List<PurchaseDetailsDTO> getPurchasesByUserId(Long userId) {
        log.info(AppConstants.SERVICE_LOG_PREFIX + "Fetching purchases by user ID: {}", userId);

        try {
            return purchaseRepository.findByUser_UserId(userId).stream().filter(PurchaseDetails::getIsActive)
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());

        } catch (IllegalArgumentException e) {
            log.error(AppConstants.SERVICE_LOG_PREFIX + "Invalid user ID: {}", userId, e);
            throw e;
        } catch (DataIntegrityViolationException e) {
            log.error(AppConstants.SERVICE_LOG_PREFIX + "Data integrity violation while fetching purchases for user ID: {}", userId, e);
            throw new RuntimeException("Data integrity error: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            log.error(AppConstants.SERVICE_LOG_PREFIX + "Runtime exception while fetching purchases for user ID: {}", userId, e);
            throw e;
        }
    }

    //==============================================update Purchase By purchase Details Id =========================================================================
    @Override
    public PurchaseDetailsDTO updatePurchaseBypurchaseDetailsId(Long purchaseDetailsId, PurchaseDetailsDTO purchaseDTO) {
        log.info(AppConstants.SERVICE_LOG_PREFIX + "Updating purchase by ID: {}", purchaseDetailsId);

        try {
            PurchaseDetails existingPurchase = purchaseRepository.findById(purchaseDetailsId)
                    .orElseThrow(() -> new ResourceNotFoundException(AppConstants.PURCHASE_NOT_FOUND + purchaseDetailsId));

            // Update user if userId has changed
            if (!existingPurchase.getUser().getUserId().equals(purchaseDTO.getUserId())) {
                AppUser user = userRepository.findById(purchaseDTO.getUserId())
                        .orElseThrow(() -> new ResourceNotFoundException(AppConstants.USER_NOT_FOUND + purchaseDTO.getUserId()));
                existingPurchase.setUser(user);
                existingPurchase.setUserNameAtPurchase(user.getName()); // Update direct column
            }

            // Update product if productId has changed
            if (!existingPurchase.getProduct().getProductDetailsId().equals(purchaseDTO.getProductId())) {
                ProductDetails product = productRepository.findById(purchaseDTO.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException(AppConstants.PRODUCT_NOT_FOUND + purchaseDTO.getProductId()));
                existingPurchase.setProduct(product);
                existingPurchase.setProductNameAtPurchase(product.getProductName()); // Update direct column
            }

            // Update count and date
            existingPurchase.setProductCount(purchaseDTO.getProductCount());
            existingPurchase.setPurchaseDate(new Date()); // Optionally update purchase date to now

            PurchaseDetails updated = purchaseRepository.save(existingPurchase);
            return mapToDTO(updated);

        } catch (IllegalArgumentException e) {
            log.error(AppConstants.SERVICE_LOG_PREFIX + "Invalid input fields for purchase ID: {}", purchaseDetailsId, e);
            throw e;
        } catch (DataIntegrityViolationException e) {
            log.error(AppConstants.SERVICE_LOG_PREFIX + "Data integrity violation while updating purchase ID: {}", purchaseDetailsId, e);
            throw new RuntimeException("Data integrity error: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            log.error(AppConstants.SERVICE_LOG_PREFIX + "Runtime exception while updating purchase ID: {}", purchaseDetailsId, e);
            throw e;
        }
    }

    //==============================================delete Purchase By Purchase Details Id =========================================================================
    @Override
    public void deletePurchaseByPurchaseDetailsId(Long purchaseDetailsId) {
        log.info(AppConstants.SERVICE_LOG_PREFIX + "Deleting purchase by ID: {}", purchaseDetailsId);

        try {
            PurchaseDetails purchase = purchaseRepository.findById(purchaseDetailsId)
                    .orElseThrow(() -> new ResourceNotFoundException(AppConstants.PURCHASE_NOT_FOUND + purchaseDetailsId));
            purchase.setIsActive(false);
            purchaseRepository.save(purchase);
        } catch (IllegalArgumentException e) {
            log.error(AppConstants.SERVICE_LOG_PREFIX + "Invalid purchase details ID: {}", purchaseDetailsId, e);
            throw e;
        } catch (DataIntegrityViolationException e) {
            log.error(AppConstants.SERVICE_LOG_PREFIX + "Data integrity violation while deleting purchase ID: {}", purchaseDetailsId, e);
            throw new RuntimeException("Data integrity error: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            log.error(AppConstants.SERVICE_LOG_PREFIX + "Runtime exception while deleting purchase ID: {}", purchaseDetailsId, e);
            throw e;
        }
    }

    private PurchaseDetailsDTO mapToDTO(PurchaseDetails entity) {
        return PurchaseDetailsDTO.builder()
                .purchaseDetailsId(entity.getPurchaseDetailsId())
                .userId(entity.getUser() != null ? entity.getUser().getUserId() : null)
                .userName(entity.getUserNameAtPurchase()) // Use the directly stored name
                .productId(entity.getProduct() != null ? entity.getProduct().getProductDetailsId() : null)
                .productName(entity.getProductNameAtPurchase()) // Use the directly stored name
                .productCount(entity.getProductCount())
                .purchaseDate(entity.getPurchaseDate())
                .isActive(entity.getIsActive())
                .build();
    }
}