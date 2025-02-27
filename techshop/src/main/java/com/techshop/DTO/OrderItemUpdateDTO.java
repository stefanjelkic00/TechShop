package com.techshop.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemUpdateDTO {
    private Long id;        // ID stavke narudžbine koja se menja
    private int quantity;   // Nova količina
    private BigDecimal price; // Eventualna izmena cene
}
