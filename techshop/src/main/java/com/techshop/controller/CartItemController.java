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
                cartItem.getId(), // Dodajemo id
                cartItem.getCart().getId(),
                cartItem.getProduct().getId(),
                cartItem.getQuantity()
            ))
            .collect(Collectors.toList());
        return ResponseEntity.ok(cartItems);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCartItemById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().body("Invalid cartItemId: id must be a positive number.");
        }
        return cartItemService.getCartItemById(id)
            .map(cartItem -> new CartItemDTO(
                cartItem.getId(), // Dodajemo id
                cartItem.getCart().getId(),
                cartItem.getProduct().getId(),
                cartItem.getQuantity()
            ))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/cart/{cartId}")
    public ResponseEntity<List<CartItemDTO>> getCartItemsByCartId(@PathVariable Long cartId) {
        if (cartId == null || cartId <= 0) {
            return ResponseEntity.badRequest().build();
        }
        List<CartItem> cartItems = cartItemService.getCartItemsByCartId(cartId);
        List<CartItemDTO> cartItemDTOs = cartItems.stream()
            .map(item -> new CartItemDTO(
                item.getId(), // Dodajemo id
                item.getCart().getId(),
                item.getProduct().getId(),
                item.getQuantity()
            ))
            .collect(Collectors.toList());
        return ResponseEntity.ok(cartItemDTOs);
    }

    @PostMapping
    public ResponseEntity<?> addCartItem(@RequestBody CartItemDTO cartItemDTO) {
        if (cartItemDTO == null || cartItemDTO.getCartId() == null || cartItemDTO.getProductId() == null) {
            return ResponseEntity.badRequest().body("Invalid CartItemDTO: cartId and productId must be provided.");
        }
        try {
            CartItem cartItem = cartItemService.addCartItem(cartItemDTO);
            return ResponseEntity.ok(new CartItemDTO(
                cartItem.getId(), // Dodajemo id
                cartItem.getCart().getId(),
                cartItem.getProduct().getId(),
                cartItem.getQuantity()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Failed to add cart item: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCartItem(@PathVariable Long id, @RequestBody CartItemUpdateDTO cartItemUpdateDTO) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().body("Invalid cartItemId: id must be a positive number.");
        }
        try {
            cartItemUpdateDTO.setId(id);
            CartItem updatedCartItem = cartItemService.updateCartItem(cartItemUpdateDTO);
            return ResponseEntity.ok(new CartItemDTO(
                updatedCartItem.getId(), // Dodajemo id
                updatedCartItem.getCart().getId(),
                updatedCartItem.getProduct().getId(),
                updatedCartItem.getQuantity()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Failed to update cart item: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCartItem(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().body("Invalid cartItemId: id must be a positive number.");
        }
        try {
            cartItemService.deleteCartItem(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}