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
public class OrderItemDTO {
    private Long orderId;   // ID narudžbine kojoj pripada
    private Long productId; // ID proizvoda koji se dodaje u narudžbinu
    private int quantity;   // Količina proizvoda
    private BigDecimal price; // Cena proizvoda u trenutku kupovine
}
