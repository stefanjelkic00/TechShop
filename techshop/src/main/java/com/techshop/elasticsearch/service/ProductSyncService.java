package com.techshop.elasticsearch.service;

import com.techshop.elasticsearch.model.ProductDocument;
import com.techshop.model.Product;

public interface ProductSyncService {
    void syncProducts();
    ProductDocument convertToDocument(Product product);
    void updateProductInElasticsearch(Product product); // Dodajemo novu metodu
}