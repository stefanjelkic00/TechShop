package com.techshop.dto;

import com.techshop.enums.OrderStatus;
import com.techshop.model.Address;
import com.techshop.model.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long id;
    private BigDecimal totalPrice;
    private OrderStatus orderStatus;
    private Long userId;
    private List<OrderItem> orderItems;
    private Address address;
    private Date createdAt; 
}