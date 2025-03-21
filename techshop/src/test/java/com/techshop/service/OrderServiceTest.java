package com.techshop.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import com.techshop.dto.OrderDTO;
import com.techshop.enums.OrderStatus;
import com.techshop.model.Order;
import com.techshop.model.User;
import com.techshop.repository.OrderRepository;
import com.techshop.repository.UserRepository;
import com.techshop.serviceImplementation.OrderServiceImplementation;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderServiceImplementation orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateOrder() {
        // Kreiramo testnog korisnika
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com"); // Dodajemo email za korisnika

        // Kreiramo OrderDTO
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setUserId(1L);
        orderDTO.setTotalPrice(BigDecimal.valueOf(100));

        // Mock-ujemo ponašanje repozitorijuma
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user)); // Dodajemo mock za findByEmail
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Pozivamo metodu sa oba parametra: OrderDTO i email
        Order createdOrder = orderService.createOrder(orderDTO, "test@example.com");

        // Proveravamo rezultate
        assertNotNull(createdOrder);
        assertEquals(OrderStatus.PENDING, createdOrder.getOrderStatus());
        assertEquals(BigDecimal.valueOf(100), createdOrder.getTotalPrice());
        assertNotNull(createdOrder.getUser());
        assertEquals(user, createdOrder.getUser());
    }
}