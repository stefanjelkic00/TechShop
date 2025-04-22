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

    //@Mock
    //private UserRepository userRepository;

    @InjectMocks
    private OrderServiceImplementation orderService;
    
    private Order order; 

    @BeforeEach
    void setUp() {
       // MockitoAnnotations.openMocks(this);
        order = new Order();
        order.setId(1L);
        order.setTotalPrice(BigDecimal.valueOf(50.0));
        order.setOrderStatus(OrderStatus.DELIVERED);
        
    }

    @Test
    void testGetOrderById() {
    	when(orderService.getOrderById(1L)).thenReturn(Optional.of(order));
    	assertEquals(BigDecimal.valueOf(50.0), order.getTotalPrice());
    }
}