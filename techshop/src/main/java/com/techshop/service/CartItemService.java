package com.techshop.service;

import com.techshop.dto.CartItemDTO;
import com.techshop.dto.CartItemUpdateDTO;
import com.techshop.model.CartItem;

import java.util.List;
import java.util.Optional;

public interface CartItemService {

    List<CartItem> getAllCartItems();

    Optional<CartItem> getCartItemById(Long id);

    List<CartItem> getCartItemsByCartId(Long cartId);

    CartItem addCartItem(CartItemDTO cartItemDTO);

    CartItem updateCartItem(CartItemUpdateDTO cartItemUpdateDTO);

    void deleteCartItem(Long id);
}