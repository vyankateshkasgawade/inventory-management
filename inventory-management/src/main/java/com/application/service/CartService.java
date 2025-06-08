package com.application.service;



import java.util.List;

import com.application.dto.CartDTO;

public interface CartService 
{
    CartDTO addToCart(CartDTO cartDTO);
    
    CartDTO updateCartByCartId(Long cartId, CartDTO cartDTO);
    
    CartDTO getCartByCartId(Long cartId);
    
    List<CartDTO> getCartsByUserId(Long userId);
    
    void deleteCartByCartId(Long cartId);
    
    void clearCartByUserId(Long userId); 
}
