package com.techshop.service;

import com.techshop.dto.CartDTO;
import com.techshop.model.Cart;

import java.util.List;
import java.util.Optional;

public interface CartService {
    List<Cart> getAllCarts();
    Optional<Cart> getCartById(Long id);
    public Cart getCartByUserId(Long userId);
    Cart createCart(CartDTO cartDTO);
    
    //a ovde mozda fali cartupdate - videcemo da li ce nam trebati 
    
    void deleteCart(Long id);
}
