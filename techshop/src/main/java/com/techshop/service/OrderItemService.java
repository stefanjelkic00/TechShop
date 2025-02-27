package com.techshop.service;

import com.techshop.DTO.OrderItemDTO;
import com.techshop.DTO.OrderItemUpdateDTO;
import com.techshop.model.OrderItem;

import java.util.List;
import java.util.Optional;

public interface OrderItemService {
    List<OrderItem> getAllOrderItems();
    Optional<OrderItem> getOrderItemById(Long id);
    OrderItem addOrderItem(OrderItemDTO orderItemDTO);
    OrderItem updateOrderItem(OrderItemUpdateDTO orderItemUpdateDTO);
    void deleteOrderItem(Long id);
}
