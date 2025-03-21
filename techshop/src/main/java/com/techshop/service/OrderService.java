package com.techshop.service;

import com.techshop.dto.AddressDTO;
import com.techshop.dto.OrderDTO;
import com.techshop.dto.OrderUpdateDTO;
import com.techshop.enums.CustomerType;
import com.techshop.model.Cart;
import com.techshop.model.Order;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface OrderService {
    List<OrderDTO> getAllOrders();
    Optional<Order> getOrderById(Long id);
    Order createOrder(OrderDTO orderDTO, String email);
    Map<String, Object> createOrderFromCart(Long userId, AddressDTO addressDTO, HttpServletRequest request) throws IOException; // Ažurirano
    Order updateOrder(Long id, OrderUpdateDTO orderUpdateDTO);
    BigDecimal calculateTotalPrice(Cart cart, BigDecimal discount);
    Map<String, Object> deleteOrder(Long id, HttpServletRequest request) throws IOException; // Ažurirano
    void refreshTokenAfterOrder(Long userId, HttpServletRequest request, HttpServletResponse response, CustomerType customerType) throws IOException;
    Long getUserIdByEmail(String email);
    String getEmailByUserId(Long userId);
    List<OrderDTO> getOrdersByEmail(String email);
    OrderDTO mapToDTO(Order order);
}