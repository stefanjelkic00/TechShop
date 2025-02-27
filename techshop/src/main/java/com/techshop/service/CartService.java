package com.techshop.service;

import com.techshop.DTO.CartDTO;
import com.techshop.model.Cart;

import java.util.List;
import java.util.Optional;

public interface CartService {
    List<Cart> getAllCarts();
    Optional<Cart> getCartById(Long id);
    Cart createCart(CartDTO cartDTO);
    void deleteCart(Long id);
}
