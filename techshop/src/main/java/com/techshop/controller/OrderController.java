package com.techshop.controller;

import com.techshop.dto.AddressDTO;
import com.techshop.dto.OrderDTO;
import com.techshop.dto.OrderUpdateDTO;
import com.techshop.model.Order;
import com.techshop.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        logger.info("Pristup endpointu /api/orders/all - Dohvatanje svih porudžbina (admin)");
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OrderDTO>> getOrders() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Pristup endpointu /api/orders sa autentifikacijom: {}", authentication);

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            logger.warn("Korisnik nije autentifikovan, vraćam 401 Unauthorized");
            return ResponseEntity.status(401).body(null);
        }

        String email = authentication.getName();
        List<OrderDTO> orders = orderService.getOrdersByEmail(email);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Pristup endpointu /api/orders/{} sa autentifikacijom: {}", id, authentication);

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            logger.warn("Korisnik nije autentifikovan, vraćam 401 Unauthorized");
            return ResponseEntity.status(401).body(null);
        }

        Optional<Order> orderOptional = orderService.getOrderById(id);
        if (orderOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Order order = orderOptional.get();
        OrderDTO orderDTO = orderService.mapToDTO(order);
        return ResponseEntity.ok(orderDTO);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderDTO> createOrder(@RequestBody OrderDTO orderDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Kreiranje porudžbine sa autentifikacijom: {}", authentication);

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            logger.warn("Korisnik nije autentifikovan, vraćam 401 Unauthorized");
            return ResponseEntity.status(401).body(null);
        }

        String email = authentication.getName();
        Order order = orderService.createOrder(orderDTO, email);
        OrderDTO createdOrderDTO = orderService.mapToDTO(order);
        return ResponseEntity.ok(createdOrderDTO);
    }

    @PostMapping("/user/{userId}/checkout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> checkout(@PathVariable Long userId, @RequestBody(required = false) AddressDTO addressDTO,
                                      HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Checkout za userId: {} sa autentifikacijom: {}", userId, authentication);

        try {
            if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
                logger.warn("Korisnik nije autentifikovan, vraćam 401 Unauthorized");
                return ResponseEntity.status(401).body(null);
            }

            String email = authentication.getName();
            String userEmail = orderService.getEmailByUserId(userId);

            if (!email.equals(userEmail)) {
                logger.warn("Nedozvoljen pristup checkout-u za userId: {}, email: {}", userId, email);
                return ResponseEntity.status(403).body("Nemate dozvolu za ovu akciju.");
            }

            Map<String, Object> result = orderService.createOrderFromCart(userId, addressDTO, request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Greška prilikom checkout-a: {}", e.getMessage());
            return ResponseEntity.status(500).body("Došlo je do neočekivane greške: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDTO> updateOrder(@PathVariable Long id, @RequestBody OrderUpdateDTO orderUpdateDTO) {
        logger.info("Ažuriranje porudžbine sa ID: {}", id);
        Order updatedOrder = orderService.updateOrder(id, orderUpdateDTO);
        OrderDTO updatedOrderDTO = orderService.mapToDTO(updatedOrder);
        return ResponseEntity.ok(updatedOrderDTO);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteOrder(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.info("Brisanje porudžbine sa ID: {}", id);
        Map<String, Object> tokens = orderService.deleteOrder(id, request);
        return ResponseEntity.ok(tokens);
    }
}