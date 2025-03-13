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
        User user = new User();
        user.setId(1L);

        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setUserId(1L);
        orderDTO.setTotalPrice(BigDecimal.valueOf(100));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order createdOrder = orderService.createOrder(orderDTO);

        assertNotNull(createdOrder);
        assertEquals(OrderStatus.PENDING, createdOrder.getOrderStatus());
        assertEquals(BigDecimal.valueOf(100), createdOrder.getTotalPrice());
        assertNotNull(createdOrder.getUser());
        assertEquals(user, createdOrder.getUser());
    }
}
