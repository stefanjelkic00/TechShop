package com.techshop.serviceImplementation;

import com.techshop.DTO.OrderDTO;
import com.techshop.DTO.OrderUpdateDTO;
import com.techshop.enums.OrderStatus;
import com.techshop.model.Order;
import com.techshop.model.User;
import com.techshop.repository.OrderRepository;
import com.techshop.repository.UserRepository;
import com.techshop.service.OrderService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImplementation implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public OrderServiceImplementation(OrderRepository orderRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
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
}
