package com.techshop.serviceImplementation;

import com.techshop.DTO.OrderDTO;
import com.techshop.DTO.OrderUpdateDTO;
import com.techshop.enums.CustomerType;
import com.techshop.enums.OrderStatus;
import com.techshop.model.Cart;
import com.techshop.model.CartItem;
import com.techshop.model.Order;
import com.techshop.model.OrderItem;
import com.techshop.model.User;
import com.techshop.repository.CartItemRepository;
import com.techshop.repository.CartRepository;
import com.techshop.repository.OrderRepository;
import com.techshop.repository.UserRepository;
import com.techshop.service.OrderService;
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
    private final CartRepository cartRepository; // Dodato!


    public OrderServiceImplementation(OrderRepository orderRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
		this.cartRepository = null;
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

        Order order = Order.builder()
                .totalPrice(orderDTO.getTotalPrice())
                .orderStatus(OrderStatus.PENDING) // Default status
                .user(user)
                .build();

        return orderRepository.save(order);
    }
    
    @Override
    @Transactional
    public Order createOrderFromCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)  // Ispravljeno
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

        cart.getCartItems().clear(); // Prazni korpu nakon narudžbine
        cartRepository.save(cart);  // Ažurira praznu korpu

        return orderRepository.save(order);
    }



    @Override
    public Order updateOrder(Long id, OrderUpdateDTO orderUpdateDTO) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

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

        User user = cart.getUser(); // Dohvatanje korisnika iz korpe
        long orderCount = orderRepository.countByUser(user); // Broj porudžbina korisnika

        BigDecimal discount = BigDecimal.ZERO;
        
        if (orderCount >= 5) {
            user.setCustomerType(CustomerType.VIP);
            discount = BigDecimal.valueOf(0.30); // 30% popusta
        } else if (orderCount >= 3) {
            user.setCustomerType(CustomerType.PLATINUM);
            discount = BigDecimal.valueOf(0.20); // 20% popusta
        } else if (orderCount >= 1) {
            user.setCustomerType(CustomerType.PREMIUM);
            discount = BigDecimal.valueOf(0.10); // 10% popusta
        } else {
            user.setCustomerType(CustomerType.REGULAR);
        }

        userRepository.save(user); // Ažuriranje korisnika u bazi

        return totalPrice.subtract(totalPrice.multiply(discount));
    }


    
    
}
