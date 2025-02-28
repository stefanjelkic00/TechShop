package com.techshop.dto;

import com.techshop.enums.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderUpdateDTO {
    private Long id;
    private BigDecimal totalPrice;
    private OrderStatus orderStatus;


    // Getters & Setters
    
}
