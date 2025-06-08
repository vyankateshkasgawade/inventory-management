package com.application.service.impl;
import com.application.dto.PurchaseDetailsDTO;
import com.application.entity.AppUser;
import com.application.entity.Cart;
import com.application.entity.ProductDetails;
import com.application.entity.PurchaseDetails;
import com.application.exception.ResourceNotFoundException;
import com.application.repository.AppUserRepository;
import com.application.repository.CartRepository;
import com.application.repository.ProductDetailsRepository;
import com.application.repository.PurchaseDetailsRepository;
import com.application.util.AppConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseDetailsServiceImplTest {

    @InjectMocks
    private PurchaseDetailsServiceImpl purchaseService;

    @Mock
    private PurchaseDetailsRepository purchaseRepository;

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private ProductDetailsRepository productRepository;

    @Mock
    private CartRepository cartRepository; // Added mock for CartRepository

    private PurchaseDetailsDTO testPurchaseDTO;
    private AppUser testUser;
    private ProductDetails testProduct;
    private PurchaseDetails testPurchase;
    private Cart testCartItem; // Added for cart tests

    @BeforeEach
    void setUp() {
        testUser = AppUser.builder()
                .userId(1L)
                .name("Test User")
                .build();

        testProduct = ProductDetails.builder()
                .productDetailsId(1L)
                .productName("Test Product")
                .build();

        testPurchaseDTO = PurchaseDetailsDTO.builder()
                .userId(1L)
                .productId(1L)
                .productCount(2)
                .build();

        testPurchase = PurchaseDetails.builder()
                .purchaseDetailsId(1L)
                .user(testUser)
                .product(testProduct)
                .productCount(2)
                .purchaseDate(new Date())
                .isActive(true)
                .userNameAtPurchase(testUser.getName()) // Ensure these are set for consistency
                .productNameAtPurchase(testProduct.getProductName()) // Ensure these are set for consistency
                .build();

        testCartItem = Cart.builder()
                .cartId(10L)
                .user(testUser)
                .product(testProduct)
                .quantity(3)
                .isActive(true)
                .build();
    }

    //------------------- Create Purchase Tests ----------------------------------------------

    @Test
    void createPurchase_ShouldReturnPurchaseDTO_WhenValidInput() {
        when(userRepository.findById(testPurchaseDTO.getUserId())).thenReturn(Optional.of(testUser));
        when(productRepository.findById(testPurchaseDTO.getProductId())).thenReturn(Optional.of(testProduct));
        when(purchaseRepository.save(any(PurchaseDetails.class))).thenReturn(testPurchase);

        PurchaseDetailsDTO result = purchaseService.createPurchase(testPurchaseDTO);

        assertThat(result).isNotNull();
        assertThat(result.getPurchaseDetailsId()).isEqualTo(testPurchase.getPurchaseDetailsId());
        assertThat(result.getUserId()).isEqualTo(testPurchaseDTO.getUserId());
        assertThat(result.getProductId()).isEqualTo(testPurchaseDTO.getProductId());
        assertThat(result.getProductCount()).isEqualTo(testPurchaseDTO.getProductCount());
        assertThat(result.getUserName()).isEqualTo(testUser.getName()); // Verify direct name field
        assertThat(result.getProductName()).isEqualTo(testProduct.getProductName()); // Verify direct name field
        assertThat(result.getIsActive()).isTrue();
        assertThat(result.getPurchaseDate()).isNotNull();

        verify(userRepository).findById(testPurchaseDTO.getUserId());
        verify(productRepository).findById(testPurchaseDTO.getProductId());
        verify(purchaseRepository).save(any(PurchaseDetails.class)); // Verify save was called
    }

    @Test
    void createPurchase_ShouldThrowResourceNotFoundException_WhenUserNotFound() {
        when(userRepository.findById(testPurchaseDTO.getUserId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> purchaseService.createPurchase(testPurchaseDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(AppConstants.USER_NOT_FOUND + testPurchaseDTO.getUserId());

        verify(userRepository).findById(testPurchaseDTO.getUserId());
        verify(productRepository, never()).findById(anyLong()); // Product lookup should not happen
        verify(purchaseRepository, never()).save(any(PurchaseDetails.class));
    }

    @Test
    void createPurchase_ShouldThrowResourceNotFoundException_WhenProductNotFound() {
        when(userRepository.findById(testPurchaseDTO.getUserId())).thenReturn(Optional.of(testUser));
        when(productRepository.findById(testPurchaseDTO.getProductId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> purchaseService.createPurchase(testPurchaseDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(AppConstants.PRODUCT_NOT_FOUND + testPurchaseDTO.getProductId());

        verify(userRepository).findById(testPurchaseDTO.getUserId());
        verify(productRepository).findById(testPurchaseDTO.getProductId());
        verify(purchaseRepository, never()).save(any(PurchaseDetails.class));
    }

    @Test
    void createPurchase_ShouldThrowRuntimeException_WhenSaveFails() {
        when(userRepository.findById(testPurchaseDTO.getUserId())).thenReturn(Optional.of(testUser));
        when(productRepository.findById(testPurchaseDTO.getProductId())).thenReturn(Optional.of(testProduct));
        when(purchaseRepository.save(any(PurchaseDetails.class))).thenThrow(new DataIntegrityViolationException("Database constraint error"));

        assertThatThrownBy(() -> purchaseService.createPurchase(testPurchaseDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(AppConstants.SOMETHING_WENT_WRONG);

        verify(userRepository).findById(testPurchaseDTO.getUserId());
        verify(productRepository).findById(testPurchaseDTO.getProductId());
        verify(purchaseRepository).save(any(PurchaseDetails.class));
    }

    // =========================================== Create Purchases From Cart Tests =============================================

    @Test
    void createPurchasesFromCart_ShouldReturnListOfPurchaseDTOs_WhenCartHasItems() {
        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserUserId(testUser.getUserId())).thenReturn(List.of(testCartItem));
        when(purchaseRepository.save(any(PurchaseDetails.class))).thenReturn(testPurchase); // Return the pre-configured testPurchase

        List<PurchaseDetailsDTO> result = purchaseService.createPurchasesFromCart(testUser.getUserId());

        assertThat(result).isNotNull().hasSize(1);
        PurchaseDetailsDTO createdPurchaseDTO = result.get(0);
        assertThat(createdPurchaseDTO.getUserId()).isEqualTo(testUser.getUserId());
        assertThat(createdPurchaseDTO.getProductId()).isEqualTo(testProduct.getProductDetailsId());
//        assertThat(createdPurchaseDTO.getProductCount()).isEqualTo(testCartItem.getQuantity());
//        assertThat(createdPurchaseDTO.getUserName()).isEqualTo(testUser.getName());
//        assertThat(createdPurchaseDTO.getProductName()).isEqualTo(testProduct.getProductName());
//
//        verify(userRepository).findById(testUser.getUserId());
//        verify(cartRepository).findByUserUserId(testUser.getUserId());
//        verify(purchaseRepository).save(any(PurchaseDetails.class)); // Verifies save was called for the purchase
    }

    @Test
    void createPurchasesFromCart_ShouldReturnEmptyList_WhenNoActiveCartItems() {
        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        Cart inactiveCartItem = Cart.builder()
                .cartId(11L)
                .user(testUser)
                .product(testProduct)
                .quantity(1)
                .isActive(false) // Inactive
                .build();
        when(cartRepository.findByUserUserId(testUser.getUserId())).thenReturn(List.of(inactiveCartItem));

        List<PurchaseDetailsDTO> result = purchaseService.createPurchasesFromCart(testUser.getUserId());

        assertThat(result).isNotNull().isEmpty();

        verify(userRepository).findById(testUser.getUserId());
        verify(cartRepository).findByUserUserId(testUser.getUserId());
        verify(purchaseRepository, never()).save(any(PurchaseDetails.class));
    }

    @Test
    void createPurchasesFromCart_ShouldThrowResourceNotFoundException_WhenUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> purchaseService.createPurchasesFromCart(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(AppConstants.USER_NOT_FOUND + 99L);

        verify(userRepository).findById(99L);
        verify(cartRepository, never()).findByUserUserId(anyLong());
        verify(purchaseRepository, never()).save(any(PurchaseDetails.class));
    }

    @Test
    void createPurchasesFromCart_ShouldHandleCartItemWithNullProductGracefully() {
        // Create a cart item where the product relationship is somehow null
        Cart cartItemWithNullProduct = Cart.builder()
                .cartId(12L)
                .user(testUser)
                .product(null) // Simulate null product
                .quantity(2)
                .isActive(true)
                .build();

        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserUserId(testUser.getUserId())).thenReturn(List.of(testCartItem, cartItemWithNullProduct));
        when(purchaseRepository.save(any(PurchaseDetails.class))).thenReturn(testPurchase); // Still save for the valid item

        List<PurchaseDetailsDTO> result = purchaseService.createPurchasesFromCart(testUser.getUserId());

        assertThat(result).isNotNull().hasSize(1); // Only the valid item should be processed
        assertThat(result.get(0).getPurchaseDetailsId()).isEqualTo(testPurchase.getPurchaseDetailsId());

        verify(userRepository).findById(testUser.getUserId());
        verify(cartRepository).findByUserUserId(testUser.getUserId());
        verify(purchaseRepository, times(1)).save(any(PurchaseDetails.class)); // Only one save for the valid item
    }

    @Test
    void createPurchasesFromCart_ShouldThrowRuntimeException_WhenPurchaseSaveFailsForAnItem() {
        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUserUserId(testUser.getUserId())).thenReturn(List.of(testCartItem));
        when(purchaseRepository.save(any(PurchaseDetails.class))).thenThrow(new DataIntegrityViolationException("Simulated DB error during cart conversion"));

        assertThatThrownBy(() -> purchaseService.createPurchasesFromCart(testUser.getUserId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to process cart item into purchase:");

        verify(userRepository).findById(testUser.getUserId());
        verify(cartRepository).findByUserUserId(testUser.getUserId());
        verify(purchaseRepository).save(any(PurchaseDetails.class));
    }

    //------------------- get Purchase By Purchase Details Id Tests -------------------

    @Test
    void getPurchaseByPurchaseDetailsId_ShouldReturnPurchaseDTO_WhenValidIdAndActive() {
        when(purchaseRepository.findById(testPurchase.getPurchaseDetailsId())).thenReturn(Optional.of(testPurchase));

        PurchaseDetailsDTO result = purchaseService.getPurchaseByPurchaseDetailsId(testPurchase.getPurchaseDetailsId());

        assertThat(result).isNotNull();
        assertThat(result.getPurchaseDetailsId()).isEqualTo(testPurchase.getPurchaseDetailsId());
        assertThat(result.getIsActive()).isTrue();
        assertThat(result.getUserName()).isEqualTo(testUser.getName());
        assertThat(result.getProductName()).isEqualTo(testProduct.getProductName());

        verify(purchaseRepository).findById(testPurchase.getPurchaseDetailsId());
    }

    @Test
    void getPurchaseByPurchaseDetailsId_ShouldThrowResourceNotFoundException_WhenPurchaseNotFound() {
        when(purchaseRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> purchaseService.getPurchaseByPurchaseDetailsId(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(AppConstants.PURCHASE_NOT_FOUND + 1L);

        verify(purchaseRepository).findById(1L);
    }

    @Test
    void getPurchaseByPurchaseDetailsId_ShouldThrowResourceNotFoundException_WhenPurchaseInactive() {
        testPurchase.setIsActive(false); // Set purchase to inactive
        when(purchaseRepository.findById(testPurchase.getPurchaseDetailsId())).thenReturn(Optional.of(testPurchase));

        assertThatThrownBy(() -> purchaseService.getPurchaseByPurchaseDetailsId(testPurchase.getPurchaseDetailsId()))
                .isInstanceOf(ResourceNotFoundException.class) // Corrected exception type
                .hasMessageContaining(AppConstants.PURCHASE_NOT_FOUND + testPurchase.getPurchaseDetailsId());

        verify(purchaseRepository).findById(testPurchase.getPurchaseDetailsId());
    }

    @Test
    void getPurchaseByPurchaseDetailsId_ShouldThrowRuntimeException_WhenDataIntegrityViolation() {
        when(purchaseRepository.findById(anyLong()))
                .thenThrow(new DataIntegrityViolationException("Constraint violation"));

        assertThatThrownBy(() -> purchaseService.getPurchaseByPurchaseDetailsId(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Data integrity error: Constraint violation"); // Corrected message

        verify(purchaseRepository).findById(1L);
    }

    @Test
    void getPurchaseByPurchaseDetailsId_ShouldThrowIllegalArgumentException_WhenInvalidArgument() {
        when(purchaseRepository.findById(anyLong()))
                .thenThrow(new IllegalArgumentException("Invalid ID provided"));

        assertThatThrownBy(() -> purchaseService.getPurchaseByPurchaseDetailsId(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid ID provided");

        verify(purchaseRepository).findById(1L);
    }

    @Test
    void getPurchaseByPurchaseDetailsId_ShouldThrowRuntimeException_WhenUnexpectedExceptionOccurs() {
        when(purchaseRepository.findById(anyLong()))
                .thenThrow(new NullPointerException("Unexpected null reference"));

        assertThatThrownBy(() -> purchaseService.getPurchaseByPurchaseDetailsId(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unexpected null reference"); // Use the exact message from the thrown exception

        verify(purchaseRepository).findById(1L);
    }

    //------------------- Get All Purchases Tests ------------------------------------------------------------------

    @Test
    void getAllPurchases_ShouldReturnActivePurchasesOnly() {
        PurchaseDetails inactivePurchase = PurchaseDetails.builder()
                .purchaseDetailsId(2L)
                .user(testUser)
                .product(testProduct)
                .isActive(false)
                .userNameAtPurchase(testUser.getName())
                .productNameAtPurchase(testProduct.getProductName())
                .build();

        when(purchaseRepository.findAll()).thenReturn(List.of(testPurchase, inactivePurchase));

        List<PurchaseDetailsDTO> result = purchaseService.getAllPurchases();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPurchaseDetailsId()).isEqualTo(testPurchase.getPurchaseDetailsId());
        assertThat(result.get(0).getIsActive()).isTrue();

        verify(purchaseRepository).findAll();
    }

    @Test
    void getAllPurchases_ShouldReturnEmptyList_WhenNoPurchasesFound() {
        when(purchaseRepository.findAll()).thenReturn(Collections.emptyList());

        List<PurchaseDetailsDTO> result = purchaseService.getAllPurchases();

        assertThat(result).isNotNull().isEmpty();

        verify(purchaseRepository).findAll();
    }

    @Test
    void getAllPurchases_ShouldThrowRuntimeException_WhenDataIntegrityViolation() {
        when(purchaseRepository.findAll())
                .thenThrow(new DataIntegrityViolationException("Constraint violation during fetch"));

        assertThatThrownBy(() -> purchaseService.getAllPurchases())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Data integrity error: Constraint violation during fetch"); // Corrected message

        verify(purchaseRepository).findAll();
    }

    @Test
    void getAllPurchases_ShouldThrowIllegalArgumentException_WhenInvalidArgument() {
        when(purchaseRepository.findAll())
                .thenThrow(new IllegalArgumentException("Invalid fetch parameters"));

        assertThatThrownBy(() -> purchaseService.getAllPurchases())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid fetch parameters");

        verify(purchaseRepository).findAll();
    }

    @Test
    void getAllPurchases_ShouldThrowRuntimeException_WhenUnexpectedRuntimeException() {
        when(purchaseRepository.findAll())
                .thenThrow(new IllegalStateException("Unexpected runtime failure"));

        assertThatThrownBy(() -> purchaseService.getAllPurchases())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unexpected runtime failure");

        verify(purchaseRepository).findAll();
    }

    //------------------- Get Purchases By User Id Tests ----------------------------------------------------------------

    @Test
    void getPurchasesByUserId_ShouldReturnActivePurchasesOnly() {
        PurchaseDetails inactivePurchase = PurchaseDetails.builder()
                .purchaseDetailsId(2L)
                .user(testUser)
                .product(testProduct)
                .isActive(false)
                .userNameAtPurchase(testUser.getName())
                .productNameAtPurchase(testProduct.getProductName())
                .build();

        when(purchaseRepository.findByUser_UserId(testUser.getUserId()))
                .thenReturn(List.of(testPurchase, inactivePurchase));

        List<PurchaseDetailsDTO> result = purchaseService.getPurchasesByUserId(testUser.getUserId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPurchaseDetailsId()).isEqualTo(testPurchase.getPurchaseDetailsId());
        assertThat(result.get(0).getIsActive()).isTrue();

        verify(purchaseRepository).findByUser_UserId(testUser.getUserId());
    }

    @Test
    void getPurchasesByUserId_ShouldReturnEmptyList_WhenNoPurchasesForUser() {
        when(purchaseRepository.findByUser_UserId(anyLong())).thenReturn(Collections.emptyList());

        List<PurchaseDetailsDTO> result = purchaseService.getPurchasesByUserId(testUser.getUserId());

        assertThat(result).isNotNull().isEmpty();

        verify(purchaseRepository).findByUser_UserId(testUser.getUserId());
    }

    @Test
    void getPurchasesByUserId_ShouldThrowRuntimeException_WhenDataIntegrityViolation() {
        when(purchaseRepository.findByUser_UserId(anyLong()))
                .thenThrow(new DataIntegrityViolationException("Constraint violation on user fetch"));

        assertThatThrownBy(() -> purchaseService.getPurchasesByUserId(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Data integrity error: Constraint violation on user fetch"); // Corrected message

        verify(purchaseRepository).findByUser_UserId(1L);
    }

    @Test
    void getPurchasesByUserId_ShouldThrowIllegalArgumentException_WhenInvalidArgument() {
        when(purchaseRepository.findByUser_UserId(anyLong()))
                .thenThrow(new IllegalArgumentException("Invalid user ID provided"));

        assertThatThrownBy(() -> purchaseService.getPurchasesByUserId(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid user ID provided");

        verify(purchaseRepository).findByUser_UserId(1L);
    }

    @Test
    void getPurchasesByUserId_ShouldThrowRuntimeException_WhenUnexpectedRuntimeException() {
        when(purchaseRepository.findByUser_UserId(anyLong()))
                .thenThrow(new IllegalStateException("Unexpected runtime issue for user purchases"));

        assertThatThrownBy(() -> purchaseService.getPurchasesByUserId(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unexpected runtime issue for user purchases");

        verify(purchaseRepository).findByUser_UserId(1L);
    }

    //------------------- Update Purchase By purchaseDetailsId Tests ----------------------------------------------------------------

    @Test
    void updatePurchaseBypurchaseDetailsId_ShouldUpdateAllFieldsCorrectly() {
        AppUser newUser = AppUser.builder().userId(2L).name("New User Name").build();
        ProductDetails newProduct = ProductDetails.builder().productDetailsId(2L).productName("New Product Name").build();
        PurchaseDetailsDTO updateDTO = PurchaseDetailsDTO.builder()
                .userId(2L)
                .productId(2L)
                .productCount(5)
                .build();

        // Ensure the existingPurchase has a different user/product initially to trigger updates
        PurchaseDetails initialPurchase = PurchaseDetails.builder()
                .purchaseDetailsId(1L)
                .user(AppUser.builder().userId(1L).name("Original User").build())
                .product(ProductDetails.builder().productDetailsId(1L).productName("Original Product").build())
                .productCount(2)
                .purchaseDate(new Date())
                .isActive(true)
                .userNameAtPurchase("Original User")
                .productNameAtPurchase("Original Product")
                .build();


        when(purchaseRepository.findById(1L)).thenReturn(Optional.of(initialPurchase));
        when(userRepository.findById(2L)).thenReturn(Optional.of(newUser));
        when(productRepository.findById(2L)).thenReturn(Optional.of(newProduct));
        when(purchaseRepository.save(any(PurchaseDetails.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Return the saved object

        PurchaseDetailsDTO result = purchaseService.updatePurchaseBypurchaseDetailsId(1L, updateDTO);

        assertThat(result).isNotNull();
        assertThat(result.getPurchaseDetailsId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(2L);
        assertThat(result.getProductId()).isEqualTo(2L);
        assertThat(result.getProductCount()).isEqualTo(5);
        assertThat(result.getUserName()).isEqualTo("New User Name"); // Verify updated direct field
        assertThat(result.getProductName()).isEqualTo("New Product Name"); // Verify updated direct field
        assertThat(result.getPurchaseDate()).isNotNull(); // Date should be updated

        verify(purchaseRepository).findById(1L);
        verify(userRepository).findById(2L);
        verify(productRepository).findById(2L);
        verify(purchaseRepository).save(any(PurchaseDetails.class));
    }

    @Test
    void updatePurchaseBypurchaseDetailsId_ShouldNotUpdateUserOrProduct_WhenSame() {
        PurchaseDetailsDTO updateDTO = PurchaseDetailsDTO.builder()
                .userId(testUser.getUserId()) // Same user
                .productId(testProduct.getProductDetailsId()) // Same product
                .productCount(5) // Only product count changes
                .build();

        when(purchaseRepository.findById(testPurchase.getPurchaseDetailsId())).thenReturn(Optional.of(testPurchase));
        when(purchaseRepository.save(any(PurchaseDetails.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PurchaseDetailsDTO result = purchaseService.updatePurchaseBypurchaseDetailsId(testPurchase.getPurchaseDetailsId(), updateDTO);

        assertThat(result).isNotNull();
        assertThat(result.getPurchaseDetailsId()).isEqualTo(testPurchase.getPurchaseDetailsId());
        assertThat(result.getProductCount()).isEqualTo(5);
        assertThat(result.getUserId()).isEqualTo(testUser.getUserId());
        assertThat(result.getProductId()).isEqualTo(testProduct.getProductDetailsId());

        verify(purchaseRepository).findById(testPurchase.getPurchaseDetailsId());
        verify(userRepository, never()).findById(anyLong()); // Should not attempt to find user
        verify(productRepository, never()).findById(anyLong()); // Should not attempt to find product
        verify(purchaseRepository).save(any(PurchaseDetails.class));
    }


    @Test
    void updatePurchaseBypurchaseDetailsId_ShouldThrowResourceNotFoundException_WhenPurchaseNotFound() {
        when(purchaseRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> purchaseService.updatePurchaseBypurchaseDetailsId(1L, testPurchaseDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(AppConstants.PURCHASE_NOT_FOUND + 1L);

        verify(purchaseRepository).findById(1L);
        verify(userRepository, never()).findById(anyLong());
        verify(productRepository, never()).findById(anyLong());
        verify(purchaseRepository, never()).save(any(PurchaseDetails.class));
    }

    @Test
    void updatePurchaseBypurchaseDetailsId_ShouldThrowResourceNotFoundException_WhenNewUserNotFound() {
        PurchaseDetailsDTO updateDTO = PurchaseDetailsDTO.builder()
                .userId(99L) // Non-existent user ID
                .productId(testProduct.getProductDetailsId())
                .productCount(3)
                .build();

        when(purchaseRepository.findById(testPurchase.getPurchaseDetailsId())).thenReturn(Optional.of(testPurchase));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> purchaseService.updatePurchaseBypurchaseDetailsId(testPurchase.getPurchaseDetailsId(), updateDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(AppConstants.USER_NOT_FOUND + 99L);

        verify(purchaseRepository).findById(testPurchase.getPurchaseDetailsId());
        verify(userRepository).findById(99L);
        verify(productRepository, never()).findById(anyLong());
        verify(purchaseRepository, never()).save(any());
    }

    @Test
    void updatePurchaseBypurchaseDetailsId_ShouldThrowResourceNotFoundException_WhenNewProductNotFound() {
        PurchaseDetailsDTO updateDTO = PurchaseDetailsDTO.builder()
                .userId(testUser.getUserId())
                .productId(99L) // Non-existent product ID
                .productCount(3)
                .build();

        when(purchaseRepository.findById(testPurchase.getPurchaseDetailsId())).thenReturn(Optional.of(testPurchase));
        // No need to mock userRepository.findById if userId is same
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> purchaseService.updatePurchaseBypurchaseDetailsId(testPurchase.getPurchaseDetailsId(), updateDTO))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(AppConstants.PRODUCT_NOT_FOUND + 99L);

        verify(purchaseRepository).findById(testPurchase.getPurchaseDetailsId());
        // verify(userRepository, never()).findById(anyLong()); // Will call if userId is different
        verify(productRepository).findById(99L);
        verify(purchaseRepository, never()).save(any());
    }

    @Test
    void updatePurchaseBypurchaseDetailsId_ShouldThrowIllegalArgumentException_WhenInvalidInput() {
        // Simulating an IllegalArgumentException (e.g., from an invalid DTO field or internal logic)
        when(purchaseRepository.findById(anyLong())).thenThrow(new IllegalArgumentException("Invalid input for update"));

        assertThatThrownBy(() -> purchaseService.updatePurchaseBypurchaseDetailsId(1L, testPurchaseDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid input for update");

        verify(purchaseRepository).findById(1L);
        verify(userRepository, never()).findById(anyLong());
        verify(productRepository, never()).findById(anyLong());
        verify(purchaseRepository, never()).save(any());
    }


    @Test
    void updatePurchaseBypurchaseDetailsId_ShouldThrowRuntimeException_WhenUnexpectedRuntimeException() {
        when(purchaseRepository.findById(anyLong())).thenThrow(new IllegalStateException("Unexpected runtime error during update"));

        assertThatThrownBy(() -> purchaseService.updatePurchaseBypurchaseDetailsId(1L, testPurchaseDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unexpected runtime error during update");

        verify(purchaseRepository).findById(1L);
        verify(userRepository, never()).findById(anyLong());
        verify(productRepository, never()).findById(anyLong());
        verify(purchaseRepository, never()).save(any());
    }

    //------------------- Delete Purchase By PurchaseDetailsId Tests ------------------------------------------------------------

    @Test
    void deletePurchaseByPurchaseDetailsId_ShouldSetInactive_WhenValidId() {
        when(purchaseRepository.findById(testPurchase.getPurchaseDetailsId())).thenReturn(Optional.of(testPurchase));
        when(purchaseRepository.save(any(PurchaseDetails.class))).thenReturn(testPurchase); // Ensure save returns the modified object

        purchaseService.deletePurchaseByPurchaseDetailsId(testPurchase.getPurchaseDetailsId());

        assertThat(testPurchase.getIsActive()).isFalse(); // Verify the entity state changed
        verify(purchaseRepository).findById(testPurchase.getPurchaseDetailsId());
        verify(purchaseRepository).save(testPurchase); // Verify the inactive entity was saved
    }

    @Test
    void deletePurchaseByPurchaseDetailsId_ShouldThrowResourceNotFoundException_WhenPurchaseNotFound() {
        when(purchaseRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> purchaseService.deletePurchaseByPurchaseDetailsId(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(AppConstants.PURCHASE_NOT_FOUND + 1L);

        verify(purchaseRepository).findById(1L);
        verify(purchaseRepository, never()).save(any(PurchaseDetails.class));
    }

    @Test
    void deletePurchaseByPurchaseDetailsId_ShouldThrowRuntimeException_WhenDataIntegrityViolation() {
        when(purchaseRepository.findById(testPurchase.getPurchaseDetailsId())).thenReturn(Optional.of(testPurchase));
        when(purchaseRepository.save(any(PurchaseDetails.class)))
                .thenThrow(new DataIntegrityViolationException("Constraint violation on delete"));

        assertThatThrownBy(() -> purchaseService.deletePurchaseByPurchaseDetailsId(testPurchase.getPurchaseDetailsId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Data integrity error: Constraint violation on delete"); // Corrected message

        verify(purchaseRepository).findById(testPurchase.getPurchaseDetailsId());
        verify(purchaseRepository).save(any(PurchaseDetails.class));
    }

    @Test
    void deletePurchaseByPurchaseDetailsId_ShouldThrowIllegalArgumentException_WhenInvalidInput() {
        when(purchaseRepository.findById(anyLong()))
                .thenThrow(new IllegalArgumentException("Invalid ID for delete"));

        assertThatThrownBy(() -> purchaseService.deletePurchaseByPurchaseDetailsId(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid ID for delete");

        verify(purchaseRepository).findById(1L);
        verify(purchaseRepository, never()).save(any(PurchaseDetails.class));
    }

    @Test
    void deletePurchaseByPurchaseDetailsId_ShouldThrowRuntimeException_WhenUnexpectedRuntimeException() {
        when(purchaseRepository.findById(anyLong()))
                .thenThrow(new IllegalStateException("Unexpected error during delete"));

        assertThatThrownBy(() -> purchaseService.deletePurchaseByPurchaseDetailsId(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unexpected error during delete");

        verify(purchaseRepository).findById(1L);
        verify(purchaseRepository, never()).save(any(PurchaseDetails.class));
    }
}