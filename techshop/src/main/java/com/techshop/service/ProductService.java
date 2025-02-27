package com.techshop.service;

import com.techshop.DTO.ProductDTO;
import com.techshop.DTO.ProductUpdateDTO;
import com.techshop.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    List<Product> getAllProducts();
    Optional<Product> getProductById(Long id);
    Product saveProduct(ProductDTO product);
    Product updateProduct(ProductUpdateDTO product);
    void deleteProduct(Long id);
}
