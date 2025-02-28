package com.techshop.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private Long cartId;   // ID korpe u koju se dodaje proizvod
    private Long productId; // ID proizvoda koji se dodaje u korpu
    private int quantity;   // Količina proizvoda
}
