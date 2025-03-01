package com.techshop.elasticsearch.service;

import com.techshop.elasticsearch.model.ProductDocument;

import java.util.List;

public interface ProductElasticsearchService {
    ProductDocument save(ProductDocument product);
    Iterable<ProductDocument> findAll();
    List<ProductDocument> findByName(String name);
}
