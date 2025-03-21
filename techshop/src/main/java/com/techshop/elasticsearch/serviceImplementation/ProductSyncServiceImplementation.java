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

    @Override
    public ProductDocument convertToDocument(Product product) {
        ProductDocument document = new ProductDocument();
        document.setId(product.getId().toString());
        document.setName(product.getName());
        document.setDescription(product.getDescription());
        document.setPrice(product.getPrice().doubleValue()); // Konverzija BigDecimal u Double
        document.setStockQuantity(product.getStockQuantity());
        document.setImageUrl(product.getImageUrl());
        document.setCategory(product.getCategory().name());
        return document;
    }

    // Dodajemo metodu za ažuriranje pojedinačnog proizvoda u Elasticsearch-u
    public void updateProductInElasticsearch(Product product) {
        ProductDocument document = convertToDocument(product);
        productElasticsearchRepository.save(document);
    }
}