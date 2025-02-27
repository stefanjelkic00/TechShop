package com.techshop.service;

import com.techshop.DTO.OrderDTO;
import com.techshop.DTO.OrderUpdateDTO;
import com.techshop.model.Cart;
import com.techshop.model.Order;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface OrderService {
    List<Order> getAllOrders();
    Optional<Order> getOrderById(Long id);
    Order createOrder(OrderDTO orderDTO);
    Order createOrderFromCart(Long userId);
    Order updateOrder(Long id, OrderUpdateDTO orderUpdateDTO);
    void deleteOrder(Long id);
    
    BigDecimal calculateTotalPrice(Cart cart);
}
