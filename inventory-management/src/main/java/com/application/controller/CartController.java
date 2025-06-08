package com.application.controller;

import com.application.dto.CartDTO;
import com.application.exception.ResourceNotFoundException;
import com.application.service.CartService;
import com.application.util.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;
//@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "CartController", description = "Operations related to Cart management")
public class CartController {

    private final CartService cartService;
    
//=========================================add Item To Cart=====================================================================
    
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @PostMapping
    @Operation(summary = "Add Item to Cart", description = "Add a new item to the user's shopping cart")
    public ResponseEntity<?> addItemToCart(@RequestBody CartDTO cartDTO) {
        log.info(AppConstants.CONTROLLER_LOG_PREFIX + "Adding item to cart: {}", cartDTO);
        try {
            CartDTO addedCartItem = cartService.addToCart(cartDTO);
            return new ResponseEntity<>(addedCartItem, HttpStatus.CREATED);

        } 
        catch (IllegalArgumentException e)
        {
            log.warn("Invalid cart input: {}", e.getMessage(), e);
            return new ResponseEntity<>("Invalid input: " + e.getMessage(), HttpStatus.BAD_REQUEST);

        } catch (DataIntegrityViolationException e)
        {
            log.warn("Cart item violates DB constraints: {}", e.getMessage(), e);
            return new ResponseEntity<>("Conflict: DB constraint violation", HttpStatus.CONFLICT);

        } catch (ResourceNotFoundException e) 
        {
            log.error("Resource not found: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);

        } catch (RuntimeException e)
        {
            if (e.getMessage().startsWith("Product with ID")) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
            } else
            {
                log.error(AppConstants.CONTROLLER_LOG_PREFIX + "Runtime error adding to cart: {}", e.getMessage(), e);
                return new ResponseEntity<>(AppConstants.FAILED + ": " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } 
    }
    
    
    
    //=========================================get Cart Item By Cart Id==============================================================
    
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/{cartId}")
    @Operation(summary = "Get Cart Item by ID", description = "Fetch a cart item by its unique cart ID")
    public ResponseEntity<?> getCartItemByCartId(@PathVariable Long cartId) {
        log.info(AppConstants.CONTROLLER_LOG_PREFIX + "Fetching cart item with ID: {}", cartId);
        try {
            CartDTO cartItem = cartService.getCartByCartId(cartId);
            return new ResponseEntity<>(cartItem, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid cart ID fetch request: {}", e.getMessage(), e);
            return new ResponseEntity<>("Invalid cart ID", HttpStatus.BAD_REQUEST);
        } catch (ResourceNotFoundException e) {
            log.error("Resource not found: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (RuntimeException e) {
            log.error("Runtime error fetching cart item ID {}: {}", cartId, e.getMessage(), e);
            return new ResponseEntity<>(AppConstants.FAILED + ": " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } 
    }
    
    
    
//====================================================get All Cart Items By User Id=========================================================================
    
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get All Cart Items for User", description = "Fetch all cart items for a user by user ID")
    public ResponseEntity<?> getAllCartItemsByUserId(@PathVariable Long userId) {
        log.info(AppConstants.CONTROLLER_LOG_PREFIX + "Fetching cart items for user ID: {}", userId);
        try {
            List<CartDTO> cartItems = cartService.getCartsByUserId(userId);
            return new ResponseEntity<>(cartItems, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid user ID for cart fetch: {}", e.getMessage(), e);
            return new ResponseEntity<>("Invalid user ID", HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            log.error("Runtime error fetching cart for user ID {}: {}", userId, e.getMessage(), e);
            return new ResponseEntity<>(AppConstants.FAILED + ": " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } 
    }
    
    
//================================================update Cart By Cart Id==========================================================================
    
    
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/{cartId}")
    @Operation(summary = "Update Cart Item", description = "Update an existing item in the cart by cart ID")
    public ResponseEntity<?> updateCartByCartId(@PathVariable Long cartId, @RequestBody CartDTO cartDTO) {
        log.info(AppConstants.CONTROLLER_LOG_PREFIX + "Updating cart item with ID {}: {}", cartId, cartDTO);
        try {
            CartDTO updatedCartItem = cartService.updateCartByCartId(cartId, cartDTO);
            return new ResponseEntity<>(updatedCartItem, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid cart update request: {}", e.getMessage(), e);
            return new ResponseEntity<>("Invalid input: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (ResourceNotFoundException e) {
            log.error("Resource not found: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } 
    }


//==========================================delete Cart Item By Cart Id==========================================================================
    
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{cartId}")
    @Operation(summary = "Delete Cart Item", description = "Delete a cart item by its unique cart ID")
    public ResponseEntity<?> deleteCartItemByCartId(@PathVariable Long cartId)
    {
        log.info(AppConstants.CONTROLLER_LOG_PREFIX + "Deleting cart item with ID: {}", cartId);
        try {
            cartService.deleteCartByCartId(cartId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) 
        {
            log.warn("Invalid cart ID during delete: {}", e.getMessage(), e);
            return new ResponseEntity<>("Invalid cart ID", HttpStatus.BAD_REQUEST);
       
        } catch (RuntimeException e) 
        {
            log.error("Runtime error deleting cart ID {}: {}", cartId, e.getMessage(), e);
            return new ResponseEntity<>(AppConstants.FAILED + ": " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } 
    }
}
