package com.techshop.serviceImplementation;

import com.techshop.dto.OrderItemDTO;
import com.techshop.dto.OrderItemUpdateDTO;
import com.techshop.model.Order;
import com.techshop.model.OrderItem;
import com.techshop.model.Product;
import com.techshop.repository.OrderItemRepository;
import com.techshop.repository.OrderRepository;
import com.techshop.repository.ProductRepository;
import com.techshop.service.OrderItemService;
import com.techshop.elasticsearch.service.ProductSyncService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderItemServiceImplementation implements OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductSyncService productSyncService; // Dodajemo ProductSyncService

    public OrderItemServiceImplementation(OrderItemRepository orderItemRepository, OrderRepository orderRepository,
                                          ProductRepository productRepository, ProductSyncService productSyncService) {
        this.orderItemRepository = orderItemRepository;
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.productSyncService = productSyncService;
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
            Product prod = product.get();
            if (prod.getStockQuantity() < orderItemDTO.getQuantity()) {
                throw new RuntimeException("Not enough stock for product: " + prod.getName());
            }

            prod.setStockQuantity(prod.getStockQuantity() - orderItemDTO.getQuantity());
            productRepository.save(prod);

            // Sinhronizuj sa Elasticsearch-om
            productSyncService.updateProductInElasticsearch(prod);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order.get());
            orderItem.setProduct(prod);
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
            Product product = orderItem.getProduct();

            // Ako se količina menja, ažuriraj zalihe
            int quantityDifference = orderItemUpdateDTO.getQuantity() - orderItem.getQuantity();
            if (quantityDifference != 0) {
                int newStockQuantity = product.getStockQuantity() - quantityDifference;
                if (newStockQuantity < 0) {
                    throw new RuntimeException("Not enough stock for product: " + product.getName());
                }
                product.setStockQuantity(newStockQuantity);
                productRepository.save(product);

                // Sinhronizuj sa Elasticsearch-om
                productSyncService.updateProductInElasticsearch(product);
            }

            orderItem.setQuantity(orderItemUpdateDTO.getQuantity());
            orderItem.setPrice(orderItemUpdateDTO.getPrice());
            return orderItemRepository.save(orderItem);
        } else {
            throw new RuntimeException("OrderItem not found!");
        }
    }

    @Override
    public void deleteOrderItem(Long id) {
        Optional<OrderItem> orderItemOptional = orderItemRepository.findById(id);
        if (orderItemOptional.isPresent()) {
            OrderItem orderItem = orderItemOptional.get();
            Product product = orderItem.getProduct();

            // Vrati zalihe
            product.setStockQuantity(product.getStockQuantity() + orderItem.getQuantity());
            productRepository.save(product);

            // Sinhronizuj sa Elasticsearch-om
            productSyncService.updateProductInElasticsearch(product);

            orderItemRepository.deleteById(id);
        } else {
            throw new RuntimeException("OrderItem not found!");
        }
    }
}