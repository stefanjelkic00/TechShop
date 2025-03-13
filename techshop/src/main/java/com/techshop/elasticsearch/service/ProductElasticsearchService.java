package com.techshop.elasticsearch.service;

import com.techshop.elasticsearch.model.ProductDocument;
import java.util.List;

public interface ProductElasticsearchService {
    ProductDocument save(ProductDocument product);
    Iterable<ProductDocument> findAll();
    List<ProductDocument> findByName(String name);
    
    List<ProductDocument> searchProducts(String query);
    List<ProductDocument> searchWithNormalization(String query);
    String normalizeText(String text);
    List<ProductDocument> fuzzySearch(String query);
    List<ProductDocument> searchAndSort(String query, String sortBy, String sortOrder);
    List<String> autocomplete(String query);
    List<ProductDocument> filterProducts(String category, Float minPrice, Float maxPrice, String sortBy, String sortOrder, String query);
}