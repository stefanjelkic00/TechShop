package com.techshop.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItemUpdateDTO {
    private Long id;      // ID cartItem-a koji se ažurira
    private int quantity; // Nova količina proizvoda
}
