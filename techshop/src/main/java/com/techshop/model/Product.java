package com.techshop.model;

import jakarta.persistence.*;
import lombok.*;
import com.techshop.enums.Category;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now(); // Automatski postavlja datum pri kreiranju

 
}
