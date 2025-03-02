package com.techshop.elasticsearch.model;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;


@Document(indexName = "products")
@Getter 
@Setter 
@NoArgsConstructor(force = true) // Generiše prazan konstruktor i inicijalizuje polja na podrazumevane vrednosti
@AllArgsConstructor 
@Builder
public class ProductDocument {
    @Id
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private int stockQuantity;
    private String category;
    private String imageUrl;
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
}
