package com.techshop.controller;

import com.techshop.dto.CartItemDTO;
import com.techshop.dto.CartItemUpdateDTO;
import com.techshop.model.CartItem;
import com.techshop.service.CartItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cart-items")
public class CartItemController {

    private final CartItemService cartItemService;

    public CartItemController(CartItemService cartItemService) {
        this.cartItemService = cartItemService;
    }

    @GetMapping
    public ResponseEntity<List<CartItemDTO>> getAllCartItems() {
        List<CartItemDTO> cartItems = cartItemService.getAllCartItems()
            .stream()
            .map(cartItem -> new CartItemDTO(
                cartItem.getCart().getId(),
                cartItem.getProduct().getId(),
                cartItem.getQuantity()
            ))
            .collect(Collectors.toList());
        return ResponseEntity.ok(cartItems);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CartItemDTO> getCartItemById(@PathVariable Long id) {
        return cartItemService.getCartItemById(id)
            .map(cartItem -> new CartItemDTO(
                cartItem.getCart().getId(),
                cartItem.getProduct().getId(),
                cartItem.getQuantity()
            ))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CartItem> addCartItem(@RequestBody CartItemDTO cartItemDTO) {
        CartItem cartItem = cartItemService.addCartItem(cartItemDTO);
        return ResponseEntity.ok(cartItem);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CartItem> updateCartItem(@PathVariable Long id, @RequestBody CartItemUpdateDTO cartItemUpdateDTO) {
        cartItemUpdateDTO.setId(id);
        CartItem updatedCartItem = cartItemService.updateCartItem(cartItemUpdateDTO);
        return ResponseEntity.ok(updatedCartItem);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCartItem(@PathVariable Long id) {
        cartItemService.deleteCartItem(id);
        return ResponseEntity.noContent().build();
    }
}
