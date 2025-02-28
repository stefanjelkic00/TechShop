package com.techshop.serviceImplementation;

import com.techshop.dto.OrderDTO;
import com.techshop.dto.OrderUpdateDTO;
import com.techshop.enums.CustomerType;
import com.techshop.enums.OrderStatus;
import com.techshop.model.*;
import com.techshop.repository.*;
import com.techshop.service.OrderService;
import com.techshop.service.EmailService; // Dodaj email servis

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImplementation implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository; // Dodato
    private final OrderItemRepository orderItemRepository; // Dodato
    private final EmailService emailService; // Dodato

    public OrderServiceImplementation(OrderRepository orderRepository, UserRepository userRepository,
                                      CartRepository cartRepository, CartItemRepository cartItemRepository,
                                      ProductRepository productRepository, OrderItemRepository orderItemRepository,
                                      EmailService emailService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.orderItemRepository = orderItemRepository;
        this.emailService = emailService;
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    public Order createOrder(OrderDTO orderDTO) {
        User user = userRepository.findById(orderDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Kreiranje narudžbine
        Order order = Order.builder()
                .totalPrice(orderDTO.getTotalPrice())
                .orderStatus(OrderStatus.PENDING)
                .user(user)
                .build();

        order = orderRepository.save(order);

        // Prolazimo kroz proizvode iz korpe i prebacujemo ih u narudžbinu
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        for (CartItem cartItem : cart.getCartItems()) {
            Product product = cartItem.getProduct();

            // Provera da li ima dovoljno na stanju
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Not enough stock for product: " + product.getName());
            }

            // Smanjenje količine na stanju
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            // Kreiranje stavke narudžbine
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setPrice(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            orderItemRepository.save(orderItem);
        }

        // Brisanje korpe nakon checkout-a
        cartItemRepository.deleteAll(cart.getCartItems());
        cartRepository.delete(cart);

        // Slanje emaila korisniku
        emailService.sendOrderConfirmation(user.getEmail(), order);

        return order;
    }

    @Override
    @Transactional
    public Order createOrderFromCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        Order order = Order.builder()
                .user(cart.getUser())
                .totalPrice(calculateTotalPrice(cart))
                .orderStatus(OrderStatus.PENDING)
                .orderItems(new ArrayList<>())
                .build();

        for (CartItem cartItem : cart.getCartItems()) {
            order.getOrderItems().add(new OrderItem(order, cartItem.getProduct(), cartItem.getQuantity()));
        }

        cart.getCartItems().clear();
        cartRepository.save(cart);

        return orderRepository.save(order);
    }

    @Override
    public Order updateOrder(Long id, OrderUpdateDTO orderUpdateDTO) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getOrderStatus() == OrderStatus.SHIPPED || order.getOrderStatus() == OrderStatus.DELIVERED) {
            throw new RuntimeException("Order cannot be modified after it has been shipped or delivered.");
        }

        order.setTotalPrice(orderUpdateDTO.getTotalPrice());
        order.setOrderStatus(orderUpdateDTO.getOrderStatus());

        return orderRepository.save(order);
    }

    @Override
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    @Override
    public BigDecimal calculateTotalPrice(Cart cart) {
        BigDecimal totalPrice = cart.getCartItems().stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        User user = cart.getUser();
        long orderCount = orderRepository.countByUser(user);

        BigDecimal discount = BigDecimal.ZERO;

        if (orderCount >= 5) {
            user.setCustomerType(CustomerType.VIP);
            discount = BigDecimal.valueOf(0.30);
        } else if (orderCount >= 3) {
            user.setCustomerType(CustomerType.PLATINUM);
            discount = BigDecimal.valueOf(0.20);
        } else if (orderCount >= 1) {
            user.setCustomerType(CustomerType.PREMIUM);
            discount = BigDecimal.valueOf(0.10);
        } else {
            user.setCustomerType(CustomerType.REGULAR);
        }

        userRepository.save(user);

        return totalPrice.subtract(totalPrice.multiply(discount));
    }
}
