package com.techshop.serviceImplementation;

import com.techshop.DTO.CartDTO;
import com.techshop.model.Cart;
import com.techshop.model.User;
import com.techshop.repository.CartRepository;
import com.techshop.repository.UserRepository;
import com.techshop.service.CartService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartServiceImplementation implements CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;

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
    
    public Cart getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));
    }

    @Override
    public Cart createCart(CartDTO cartDTO) {
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
