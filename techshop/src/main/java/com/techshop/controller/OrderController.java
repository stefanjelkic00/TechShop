package com.techshop.controller;

import com.techshop.dto.OrderDTO;
import com.techshop.dto.OrderUpdateDTO;
import com.techshop.model.Order;
import com.techshop.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        List<OrderDTO> orders = orderService.getAllOrders()
            .stream()
            .map(order -> new OrderDTO(
                order.getTotalPrice(),
                order.getOrderStatus(),
                order.getUser().getId()
            ))
            .collect(Collectors.toList());
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id)
            .map(order -> new OrderDTO(
                order.getTotalPrice(),
                order.getOrderStatus(),
                order.getUser().getId()
            ))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }


    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody OrderDTO orderDTO) {
        return ResponseEntity.ok(orderService.createOrder(orderDTO));
    }
    
    @PostMapping("/user/{userId}/checkout")
    public ResponseEntity<Order> checkout(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.createOrderFromCart(userId));
    }


    @PutMapping("/{id}")
    public ResponseEntity<Order> updateOrder(@PathVariable Long id, @RequestBody OrderUpdateDTO orderUpdateDTO) {
        return ResponseEntity.ok(orderService.updateOrder(id, orderUpdateDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
