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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartServiceImplTest {

    @InjectMocks
    private CartServiceImpl cartService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private ProductDetailsRepository productRepository;

    private AppUser sampleUser;
    private ProductDetails sampleProduct;
    private Cart sampleCart;
    private CartDTO sampleCartDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        sampleUser = AppUser.builder().userId(1L).name("Test User").build();
        sampleProduct = ProductDetails.builder()
                .productDetailsId(10L)
                .productName("Test Product")
                .imageUrl("http://example.com/test_product.jpg")
                .price(BigDecimal.valueOf(100))
                .build();

        sampleCart = Cart.builder()
                .cartId(100L)
                .user(sampleUser)
                .product(sampleProduct)
                .productName(sampleProduct.getProductName())
                .imageUrl(sampleProduct.getImageUrl())
                .quantity(2)
                .finalTotalAmount(BigDecimal.valueOf(200))
                .addedDate(new Date())
                .isActive(true)
                .build();

        sampleCartDTO = CartDTO.builder()
                .userId(1L)
                .productId(10L)
                .quantity(2)
                .build();
    }

    @Test
    void testAddToCart_newCartItemSuccess() {
        // Mock dependencies
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(productRepository.findById(10L)).thenReturn(Optional.of(sampleProduct));
        when(cartRepository.findByUserUserIdAndProductProductDetailsIdAndIsActiveTrue(1L, 10L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
            Cart savedCart = invocation.getArgument(0);
            savedCart.setCartId(101L); // Simulate ID generation
            return savedCart;
        });

        // Call the service method
        CartDTO result = cartService.addToCart(sampleCartDTO);

        // Verify interactions and assertions
        assertNotNull(result);
        assertEquals(101L, result.getCartId());
        assertEquals(1L, result.getUserId());
        assertEquals(10L, result.getProductId());
        assertEquals("Test Product", result.getProductName()); // Verify productName
        assertEquals("http://example.com/test_product.jpg", result.getImageUrl()); // Verify imageUrl
        assertEquals(2, result.getQuantity());
        assertEquals(BigDecimal.valueOf(200), result.getFinalTotalAmount());
        assertTrue(result.getIsActive());

        verify(userRepository).findById(1L);
        verify(productRepository).findById(10L);
        verify(cartRepository).findByUserUserIdAndProductProductDetailsIdAndIsActiveTrue(1L, 10L);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void testAddToCart_existingCartItemUpdatesQuantity() {
        // Simulate an existing cart item with quantity 1
        Cart existingCartItem = Cart.builder()
                .cartId(100L)
                .user(sampleUser)
                .product(sampleProduct)
                .productName(sampleProduct.getProductName())
                .imageUrl(sampleProduct.getImageUrl())
                .quantity(1)
                .finalTotalAmount(BigDecimal.valueOf(100))
                .addedDate(new Date())
                .isActive(true)
                .build();

        sampleCartDTO.setQuantity(2); // DTO requests adding 2 more

        // Mock dependencies
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(productRepository.findById(10L)).thenReturn(Optional.of(sampleProduct));
        when(cartRepository.findByUserUserIdAndProductProductDetailsIdAndIsActiveTrue(1L, 10L)).thenReturn(Optional.of(existingCartItem));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Return the updated cart item

        // Call the service method
        CartDTO result = cartService.addToCart(sampleCartDTO);

        // Verify interactions and assertions
        assertNotNull(result);
        assertEquals(100L, result.getCartId()); // Should be the existing cart item's ID
        assertEquals(3, result.getQuantity()); // Original (1) + new (2) = 3
        assertEquals(BigDecimal.valueOf(300), result.getFinalTotalAmount()); // 3 * 100
        assertEquals("Test Product", result.getProductName()); // Should be updated from product
        assertEquals("http://example.com/test_product.jpg", result.getImageUrl()); // Should be updated from product

        verify(userRepository).findById(1L);
        verify(productRepository).findById(10L);
        verify(cartRepository).findByUserUserIdAndProductProductDetailsIdAndIsActiveTrue(1L, 10L);
        verify(cartRepository).save(existingCartItem); // Ensure the existing item was saved
    }


    @Test
    void testAddToCart_userNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> cartService.addToCart(sampleCartDTO));

    }

    @Test
    void testAddToCart_productNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(productRepository.findById(10L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> cartService.addToCart(sampleCartDTO));
    }

    @Test
    void testAddToCart_runtimeException() {
        // Simulate a runtime exception during the save operation, for example
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(productRepository.findById(10L)).thenReturn(Optional.of(sampleProduct));
        when(cartRepository.findByUserUserIdAndProductProductDetailsIdAndIsActiveTrue(1L, 10L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenThrow(new RuntimeException("Database connection error"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> cartService.addToCart(sampleCartDTO));

        assertTrue(exception.getMessage().contains("Database connection error"));
        verify(userRepository).findById(1L);
        verify(productRepository).findById(10L);
        verify(cartRepository).findByUserUserIdAndProductProductDetailsIdAndIsActiveTrue(1L, 10L);
        verify(cartRepository).save(any(Cart.class));
    }


    @Test
    void testUpdateCartByCartId_success() {
        CartDTO updateDto = CartDTO.builder().quantity(5).imageUrl("http://example.com/updated_image.jpg").build();

        when(cartRepository.findById(100L)).thenReturn(Optional.of(sampleCart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Return the updated cart

        CartDTO result = cartService.updateCartByCartId(100L, updateDto);

        assertNotNull(result);
        assertEquals(5, result.getQuantity());
        assertEquals(BigDecimal.valueOf(500), result.getFinalTotalAmount()); // 5 * 100
        assertEquals("http://example.com/updated_image.jpg", result.getImageUrl()); // Verify imageUrl update
        verify(cartRepository).findById(100L);
        verify(cartRepository).save(sampleCart);
    }

    @Test
    void testUpdateCartByCartId_cartNotFound() {
        CartDTO updateDto = CartDTO.builder().quantity(5).build();
        when(cartRepository.findById(100L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> cartService.updateCartByCartId(100L, updateDto));
    }

    @Test
    void testUpdateCartByCartId_runtimeException() {
        CartDTO updateDto = CartDTO.builder().quantity(5).build();
        when(cartRepository.findById(100L)).thenReturn(Optional.of(sampleCart));
        // Simulate a runtime exception during the save operation
        when(cartRepository.save(any(Cart.class))).thenThrow(new RuntimeException("Database error during update"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> cartService.updateCartByCartId(100L, updateDto));

        assertTrue(exception.getMessage().contains("Database error during update"));
        verify(cartRepository).findById(100L);
        verify(cartRepository).save(any(Cart.class));
    }


    @Test
    void testGetCartByCartId_success() {
        when(cartRepository.findById(100L)).thenReturn(Optional.of(sampleCart));

        CartDTO result = cartService.getCartByCartId(100L);

        assertNotNull(result);
        assertEquals(100L, result.getCartId());
        assertEquals("Test Product", result.getProductName());
        assertEquals("http://example.com/test_product.jpg", result.getImageUrl());
        assertTrue(result.getIsActive());
        verify(cartRepository).findById(100L);
    }

    @Test
    void testGetCartByCartId_notFound() {
        when(cartRepository.findById(100L)).thenReturn(Optional.empty());

        CartNotFoundException exception = assertThrows(CartNotFoundException.class,
                () -> cartService.getCartByCartId(100L));

    }

    @Test
    void testGetCartByCartId_notActive_throwsCartNotFound() {
        sampleCart.setIsActive(false); // Simulate an inactive cart
        when(cartRepository.findById(100L)).thenReturn(Optional.of(sampleCart));

        CartNotFoundException exception = assertThrows(CartNotFoundException.class,
                () -> cartService.getCartByCartId(100L));

    }

    @Test
    void testGetCartByCartId_runtimeException() {
        when(cartRepository.findById(100L)).thenThrow(new RuntimeException("DB connection failed"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> cartService.getCartByCartId(100L));

        assertTrue(exception.getMessage().contains("DB connection failed"));
        verify(cartRepository).findById(100L);
    }


    @Test
    void testGetCartsByUserId_success() {
        Cart inactiveCart = Cart.builder()
                .cartId(102L)
                .user(sampleUser)
                .product(sampleProduct)
                .productName("Inactive Product")
                .imageUrl("inactive.jpg")
                .quantity(1)
                .finalTotalAmount(BigDecimal.valueOf(50))
                .addedDate(new Date())
                .isActive(false)
                .build();

        when(cartRepository.findByUserUserId(1L)).thenReturn(Arrays.asList(sampleCart, inactiveCart));

        List<CartDTO> carts = cartService.getCartsByUserId(1L);

        assertNotNull(carts);
        assertEquals(1, carts.size()); // Only active cart should be returned
        assertEquals(100L, carts.get(0).getCartId());
        assertEquals("Test Product", carts.get(0).getProductName());
        assertEquals("http://example.com/test_product.jpg", carts.get(0).getImageUrl());
        verify(cartRepository).findByUserUserId(1L);
    }

    @Test
    void testGetCartsByUserId_noCartsFound() {
        when(cartRepository.findByUserUserId(1L)).thenReturn(Collections.emptyList());

        List<CartDTO> carts = cartService.getCartsByUserId(1L);

        assertNotNull(carts);
        assertTrue(carts.isEmpty());
        verify(cartRepository).findByUserUserId(1L);
    }

    @Test
    void testGetCartsByUserId_runtimeException() {
        when(cartRepository.findByUserUserId(1L)).thenThrow(new RuntimeException("Network error during fetch"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> cartService.getCartsByUserId(1L));

        assertTrue(exception.getMessage().contains("Network error during fetch"));
        verify(cartRepository).findByUserUserId(1L);
    }


    @Test
    void testDeleteCartByCartId_success() {
        when(cartRepository.findById(100L)).thenReturn(Optional.of(sampleCart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        cartService.deleteCartByCartId(100L);

        assertFalse(sampleCart.getIsActive()); // Verify soft delete
        verify(cartRepository).findById(100L);
        verify(cartRepository).save(sampleCart);
    }

    @Test
    void testDeleteCartByCartId_cartNotFound() {
        when(cartRepository.findById(100L)).thenReturn(Optional.empty());

        CartNotFoundException exception = assertThrows(CartNotFoundException.class,
                () -> cartService.deleteCartByCartId(100L));


    }

    @Test
    void testDeleteCartByCartId_runtimeException() {
        when(cartRepository.findById(100L)).thenReturn(Optional.of(sampleCart));
        // Simulate a runtime exception during the save operation
        when(cartRepository.save(any(Cart.class))).thenThrow(new RuntimeException("Write error to database"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> cartService.deleteCartByCartId(100L));

        assertTrue(exception.getMessage().contains("Write error to database"));
        verify(cartRepository).findById(100L);
        verify(cartRepository).save(any(Cart.class));
    }


    @Test
    void testClearCartByUserId_success() {
        Cart cartItem1 = Cart.builder().cartId(1L).user(sampleUser).product(sampleProduct).isActive(true).build();
        Cart cartItem2 = Cart.builder().cartId(2L).user(sampleUser).product(sampleProduct).isActive(true).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(cartRepository.findByUserUserId(1L)).thenReturn(Arrays.asList(cartItem1, cartItem2));
        // Mock findById for the internal deleteCartByCartId calls
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cartItem1));
        when(cartRepository.findById(2L)).thenReturn(Optional.of(cartItem2));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        cartService.clearCartByUserId(1L);

        assertFalse(cartItem1.getIsActive());
        assertFalse(cartItem2.getIsActive());
        verify(userRepository).findById(1L);
        verify(cartRepository).findByUserUserId(1L);
        verify(cartRepository, times(2)).findById(anyLong()); // Called by deleteCartByCartId
        verify(cartRepository, times(2)).save(any(Cart.class)); // Two items saved (soft deleted)
    }

    @Test
    void testClearCartByUserId_userNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> cartService.clearCartByUserId(1L));

    
    }

    @Test
    void testClearCartByUserId_cartAlreadyEmpty() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(cartRepository.findByUserUserId(1L)).thenReturn(Collections.emptyList()); // No active carts

        cartService.clearCartByUserId(1L);

        verify(userRepository).findById(1L);
        verify(cartRepository).findByUserUserId(1L);
        verify(cartRepository, never()).save(any(Cart.class)); // No save operations should occur
    }

    @Test
    void testClearCartByUserId_runtimeException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(cartRepository.findByUserUserId(1L)).thenThrow(new RuntimeException("Failed to fetch carts"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> cartService.clearCartByUserId(1L));

        assertTrue(exception.getMessage().contains("Failed to fetch carts"));
        verify(userRepository).findById(1L);
        verify(cartRepository).findByUserUserId(1L);
        verify(cartRepository, never()).save(any(Cart.class));
    }
}