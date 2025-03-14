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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartItemServiceImplementation implements CartItemService {

    private static final Logger logger = LoggerFactory.getLogger(CartItemServiceImplementation.class);

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
    public List<CartItem> getCartItemsByCartId(Long cartId) {
        return cartItemRepository.findByCartId(cartId);
    }

    @Override
    public CartItem addCartItem(CartItemDTO cartItemDTO) {
        if (cartItemDTO.getQuantity() <= 0) {
            logger.error("Invalid quantity: {}. Quantity must be a positive number.", cartItemDTO.getQuantity());
            throw new RuntimeException("Invalid quantity: Quantity must be a positive number.");
        }

        Optional<Cart> cart = cartRepository.findById(cartItemDTO.getCartId());
        Optional<Product> product = productRepository.findById(cartItemDTO.getProductId());

        if (!cart.isPresent()) {
            logger.error("Korpa nije pronađena za cartId: {}", cartItemDTO.getCartId());
            throw new RuntimeException("Korpa nije pronađena za cartId: " + cartItemDTO.getCartId());
        }
        if (!product.isPresent()) {
            logger.error("Proizvod nije pronađen za productId: {}", cartItemDTO.getProductId());
            throw new RuntimeException("Proizvod nije pronađen za productId: " + cartItemDTO.getProductId());
        }

        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(cartItemDTO.getCartId(), cartItemDTO.getProductId());
        if (existingItem.isPresent()) {
            logger.info("Proizvod već postoji u korpi, ažuriranje količine za cartId: {}, productId: {}", 
                cartItemDTO.getCartId(), cartItemDTO.getProductId());
            CartItem cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + cartItemDTO.getQuantity());
            return cartItemRepository.save(cartItem);
        } else {
            logger.info("Kreiranje nove stavke u korpi za cartId: {}, productId: {}", 
                cartItemDTO.getCartId(), cartItemDTO.getProductId());
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart.get());
            cartItem.setProduct(product.get());
            cartItem.setQuantity(cartItemDTO.getQuantity());
            return cartItemRepository.save(cartItem);
        }
    }

    @Override
    public CartItem updateCartItem(CartItemUpdateDTO cartItemUpdateDTO) {
        if (cartItemUpdateDTO.getQuantity() <= 0) {
            logger.error("Invalid quantity: {}. Quantity must be a positive number.", cartItemUpdateDTO.getQuantity());
            throw new RuntimeException("Invalid quantity: Quantity must be a positive number.");
        }

        Optional<CartItem> cartItemOptional = cartItemRepository.findById(cartItemUpdateDTO.getId());
        if (cartItemOptional.isPresent()) {
            CartItem cartItem = cartItemOptional.get();
            cartItem.setQuantity(cartItemUpdateDTO.getQuantity());
            return cartItemRepository.save(cartItem);
        } else {
            logger.error("CartItem not found for id: {}", cartItemUpdateDTO.getId());
            throw new RuntimeException("CartItem not found for id: " + cartItemUpdateDTO.getId());
        }
    }

    @Override
    public void deleteCartItem(Long id) {
        Optional<CartItem> cartItemOptional = cartItemRepository.findById(id);
        if (cartItemOptional.isPresent()) {
            cartItemRepository.deleteById(id);
            logger.info("CartItem deleted successfully for id: {}", id);
        } else {
            logger.error("CartItem not found for id: {}", id);
            throw new RuntimeException("CartItem not found for id: " + id);
        }
    }
}