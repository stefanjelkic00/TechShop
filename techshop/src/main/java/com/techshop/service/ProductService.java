package com.techshop.service;

import com.techshop.dto.ProductDTO;
import com.techshop.dto.ProductDiscountDTO;
import com.techshop.dto.ProductUpdateDTO;
import com.techshop.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    List<Product> getAllProducts();
    Optional<Product> getProductById(Long id);
    Product saveProduct(ProductDTO product);
    Product updateProduct(ProductUpdateDTO product);
    void deleteProduct(Long id);
    
    List<ProductDiscountDTO> getProductsWithDiscount(Long userId);

}
