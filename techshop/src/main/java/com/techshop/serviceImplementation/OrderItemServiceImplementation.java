package com.techshop.serviceImplementation;

import com.techshop.DTO.OrderItemDTO;
import com.techshop.DTO.OrderItemUpdateDTO;
import com.techshop.model.Order;
import com.techshop.model.OrderItem;
import com.techshop.model.Product;
import com.techshop.repository.OrderItemRepository;
import com.techshop.repository.OrderRepository;
import com.techshop.repository.ProductRepository;
import com.techshop.service.OrderItemService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderItemServiceImplementation implements OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderItemServiceImplementation(OrderItemRepository orderItemRepository, OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderItemRepository = orderItemRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<OrderItem> getAllOrderItems() {
        return orderItemRepository.findAll();
    }

    @Override
    public Optional<OrderItem> getOrderItemById(Long id) {
        return orderItemRepository.findById(id);
    }

    @Override
    public OrderItem addOrderItem(OrderItemDTO orderItemDTO) {
        Optional<Order> order = orderRepository.findById(orderItemDTO.getOrderId());
        Optional<Product> product = productRepository.findById(orderItemDTO.getProductId());

        if (order.isPresent() && product.isPresent()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order.get());
            orderItem.setProduct(product.get());
            orderItem.setQuantity(orderItemDTO.getQuantity());
            orderItem.setPrice(orderItemDTO.getPrice());
            return orderItemRepository.save(orderItem);
        } else {
            throw new RuntimeException("Order or Product not found!");
        }
    }

    @Override
    public OrderItem updateOrderItem(OrderItemUpdateDTO orderItemUpdateDTO) {
        Optional<OrderItem> orderItemOptional = orderItemRepository.findById(orderItemUpdateDTO.getId());

        if (orderItemOptional.isPresent()) {
            OrderItem orderItem = orderItemOptional.get();
            orderItem.setQuantity(orderItemUpdateDTO.getQuantity());
            orderItem.setPrice(orderItemUpdateDTO.getPrice());
            return orderItemRepository.save(orderItem);
        } else {
            throw new RuntimeException("OrderItem not found!");
        }
    }

    @Override
    public void deleteOrderItem(Long id) {
        orderItemRepository.deleteById(id);
    }
}
