package com.techshop.elasticsearch.serviceImplementation;

import com.techshop.model.Product;
import com.techshop.repository.ProductRepository;
import com.techshop.elasticsearch.model.ProductDocument;
import com.techshop.elasticsearch.repository.ProductElasticsearchRepository;
import com.techshop.elasticsearch.service.ProductSyncService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductSyncServiceImplementation implements ProductSyncService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductElasticsearchRepository productElasticsearchRepository;

    @Override
    public void syncProducts() {
        List<Product> products = productRepository.findAll();

        List<ProductDocument> productDocuments = products.stream()
                .map(this::convertToDocument)
                .collect(Collectors.toList());

        productElasticsearchRepository.saveAll(productDocuments);
    }

    public ProductDocument convertToDocument(Product product) {
        return ProductDocument.builder()
                .id(product.getId().toString())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice().floatValue()) // Konverzija BigDecimal u Float
                .stockQuantity(product.getStockQuantity())
                .category(product.getCategory().toString()) // Enum konvertujemo u String
                .imageUrl(product.getImageUrl())
                .createdAt(product.getCreatedAt().toLocalDate()) // Konvertuj LocalDateTime u LocalDate
                .build();
    }
}