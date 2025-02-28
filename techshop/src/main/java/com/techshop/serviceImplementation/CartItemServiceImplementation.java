package com.techshop.serviceImplementation;

import com.techshop.dto.CartItemDTO;
import com.techshop.dto.CartItemUpdateDTO;
import com.techshop.model.Cart;
import com.techshop.model.CartItem;
import com.techshop.model.Product;
import com.techshop.repository.CartItemRepository;
import com.techshop.repository.CartRepository;
import com.techshop.repository.ProductRepository;
import com.techshop.service.CartItemService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartItemServiceImplementation implements CartItemService {

    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartItemServiceImplementation(CartItemRepository cartItemRepository, CartRepository cartRepository, ProductRepository productRepository) {
        this.cartItemRepository = cartItemRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<CartItem> getAllCartItems() {
        return cartItemRepository.findAll();
    }

    @Override
    public Optional<CartItem> getCartItemById(Long id) {
        return cartItemRepository.findById(id);
    }

    @Override
    public CartItem addCartItem(CartItemDTO cartItemDTO) {
        Optional<Cart> cart = cartRepository.findById(cartItemDTO.getCartId());
        Optional<Product> product = productRepository.findById(cartItemDTO.getProductId());

        if (cart.isPresent() && product.isPresent()) {
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart.get());
            cartItem.setProduct(product.get());
            cartItem.setQuantity(cartItemDTO.getQuantity());
            return cartItemRepository.save(cartItem);
        } else {
            throw new RuntimeException("Cart or Product not found!");
        }
    }

    @Override
    public CartItem updateCartItem(CartItemUpdateDTO cartItemUpdateDTO) {
        Optional<CartItem> cartItemOptional = cartItemRepository.findById(cartItemUpdateDTO.getId());

        if (cartItemOptional.isPresent()) {
            CartItem cartItem = cartItemOptional.get();
            cartItem.setQuantity(cartItemUpdateDTO.getQuantity());
            return cartItemRepository.save(cartItem);
        } else {
            throw new RuntimeException("CartItem not found!");
        }
    }

    @Override
    public void deleteCartItem(Long id) {
        cartItemRepository.deleteById(id);
    }
}
