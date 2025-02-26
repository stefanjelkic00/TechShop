package com.techshop.models;

import jakarta.persistence.*;
import lombok.*;
import com.techshop.enums.Category;
import com.techshop.DTO.ProductDTO;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    private int stockQuantity;

    @Enumerated(EnumType.STRING)
    private Category category;

    private String imageUrl;

    // Dodajemo konstruktor koji prima ProductDTO
    public Product(ProductDTO productDTO) {
        this.name = productDTO.getName();
        this.description = productDTO.getDescription();
        this.price = productDTO.getPrice();
        this.stockQuantity = productDTO.getStockQuantity();
        this.imageUrl = productDTO.getImageUrl();
        this.category = productDTO.getCategory();
    }
}
