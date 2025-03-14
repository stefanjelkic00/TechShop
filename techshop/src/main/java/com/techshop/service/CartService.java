package com.techshop.service;

import com.techshop.dto.CartDTO;
import com.techshop.model.Cart;

import java.util.List;
import java.util.Optional;

public interface CartService {
    List<Cart> getAllCarts();
    Optional<Cart> getCartById(Long id);
    CartDTO getCartByUserId(Long userId); 
    Cart createCart(CartDTO cartDTO);
    
    //a ovde mozda fali cartupdate - videcu da li ce mi trebati 
    
    void deleteCart(Long id);
}
