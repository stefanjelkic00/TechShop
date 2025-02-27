package com.techshop.service;

import com.techshop.DTO.CartItemDTO;
import com.techshop.DTO.CartItemUpdateDTO;
import com.techshop.model.CartItem;

import java.util.List;
import java.util.Optional;

public interface CartItemService {
    List<CartItem> getAllCartItems();
    Optional<CartItem> getCartItemById(Long id);
    CartItem addCartItem(CartItemDTO cartItemDTO);
    
    //ova funkcija mi mozda nece trebati jer nece biti potrebe za update cart(mocice samo da se doda ili obrise)
    CartItem updateCartItem(CartItemUpdateDTO cartItemUpdateDTO);
    
    void deleteCartItem(Long id);
    
}
