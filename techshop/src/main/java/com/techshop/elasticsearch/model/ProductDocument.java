package com.techshop.elasticsearch.model;
import lombok.*;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;


@Document(indexName = "products")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductDocument {
    @Id
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private int stockQuantity;
    private String category;
    private String imageUrl;
}
