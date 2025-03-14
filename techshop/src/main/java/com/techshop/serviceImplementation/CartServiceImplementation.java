package com.techshop.serviceImplementation;

import com.techshop.dto.CartDTO;
import com.techshop.dto.CartItemDTO;
import com.techshop.model.Cart;
import com.techshop.model.User;
import com.techshop.repository.CartRepository;
import com.techshop.repository.UserRepository;
import com.techshop.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartServiceImplementation implements CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    @Autowired
    public CartServiceImplementation(CartRepository cartRepository, UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<Cart> getAllCarts() {
        return cartRepository.findAll();
    }

    @Override
    public Optional<Cart> getCartById(Long id) {
        return cartRepository.findById(id);
    }

    @Override
    public CartDTO getCartByUserId(Long userId) {
        Optional<Cart> cartOptional = cartRepository.findByUserId(userId);
        if (cartOptional.isPresent()) {
            Cart cart = cartOptional.get();
            return new CartDTO(
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
            );
        } else {
            return new CartDTO(null, userId, null);
        }
    }

    @Override
    public Cart createCart(CartDTO cartDTO) {
        Optional<Cart> existingCart = cartRepository.findByUserId(cartDTO.getUserId());
        if (existingCart.isPresent()) {
            return existingCart.get();
        }

        Optional<User> user = userRepository.findById(cartDTO.getUserId());
        if (user.isPresent()) {
            Cart cart = new Cart();
            cart.setUser(user.get());
            return cartRepository.save(cart);
        } else {
            throw new RuntimeException("User not found with id: " + cartDTO.getUserId());
        }
    }

    @Override
    public void deleteCart(Long id) {
        cartRepository.deleteById(id);
    }
}