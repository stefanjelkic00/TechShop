package com.techshop.controller;

import com.techshop.dto.CartDTO;
import com.techshop.dto.CartItemDTO;
import com.techshop.model.Cart;
import com.techshop.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/carts")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<List<CartDTO>> getAllCarts() {
        List<CartDTO> carts = cartService.getAllCarts()
            .stream()
            .map(cart -> new CartDTO(
                cart.getId(),
                cart.getUser().getId(),
                cart.getCartItems().stream()
                    .map(item -> new CartItemDTO(
                        item.getId(), // Dodajemo id
                        item.getCart().getId(),
                        item.getProduct().getId(),
                        item.getQuantity()
                    ))
                    .collect(Collectors.toList())
            ))
            .collect(Collectors.toList());
        return ResponseEntity.ok(carts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CartDTO> getCartById(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().body(null);
        }
        return cartService.getCartById(id)
            .map(cart -> new CartDTO(
                cart.getId(),
                cart.getUser().getId(),
                cart.getCartItems().stream()
                    .map(item -> new CartItemDTO(
                        item.getId(), // Dodajemo id
                        item.getCart().getId(),
                        item.getProduct().getId(),
                        item.getQuantity()
                    ))
                    .collect(Collectors.toList())
            ))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<CartDTO> getCartByUser(@PathVariable Long userId) {
        if (userId == null || userId <= 0) {
            return ResponseEntity.badRequest().body(new CartDTO(null, null, null));
        }
        try {
            CartDTO cartDTO = cartService.getCartByUserId(userId);
            return ResponseEntity.ok(cartDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.ok(new CartDTO(null, userId, null));
        }
    }

    @PostMapping
    public ResponseEntity<?> createCart(@RequestBody CartDTO cartDTO) {
        if (cartDTO == null || cartDTO.getUserId() == null || cartDTO.getUserId() <= 0) {
            return ResponseEntity.badRequest().body("Invalid userId: userId must be a positive number.");
        }
        try {
            Cart cart = cartService.createCart(cartDTO);
            return ResponseEntity.ok(new CartDTO(
                cart.getId(),
                cart.getUser().getId(),
                cart.getCartItems().stream()
                    .map(item -> new CartItemDTO(
                        item.getId(), // Dodajemo id
                        item.getCart().getId(),
                        item.getProduct().getId(),
                        item.getQuantity()
                    ))
                    .collect(Collectors.toList())
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Failed to create cart: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCart(@PathVariable Long id) {
        if (id == null || id <= 0) {
            return ResponseEntity.badRequest().build();
        }
        try {
            cartService.deleteCart(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}