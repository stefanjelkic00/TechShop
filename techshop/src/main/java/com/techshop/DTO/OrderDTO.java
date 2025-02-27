package com.techshop.DTO;

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
public class OrderDTO {
    private BigDecimal totalPrice;
    private OrderStatus orderStatus;
    private Long userId; // Opcionalno ako želiš da znaš ko pravi narudžbinu

}
