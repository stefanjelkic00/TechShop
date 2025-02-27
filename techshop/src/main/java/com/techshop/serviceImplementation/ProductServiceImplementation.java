package com.techshop.serviceImplementation;

import com.techshop.DTO.ProductDTO;
import com.techshop.DTO.ProductUpdateDTO;
import com.techshop.model.Product;
import com.techshop.repository.ProductRepository;
import com.techshop.service.ProductService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImplementation implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImplementation(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public Product saveProduct(ProductDTO productDTO) {
        Product product = new Product();
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setStockQuantity(productDTO.getStockQuantity());
        product.setImageUrl(productDTO.getImageUrl());
        product.setCategory(productDTO.getCategory());

        return productRepository.save(product);
    }
    
    @Override
    public Product updateProduct(ProductUpdateDTO productUpdateDTO) {
        Optional<Product> existingProduct = productRepository.findById(productUpdateDTO.getId());

        if (existingProduct.isPresent()) {
            Product product = existingProduct.get();
            product.setName(productUpdateDTO.getName());
            product.setDescription(productUpdateDTO.getDescription());
            product.setPrice(productUpdateDTO.getPrice());
            product.setStockQuantity(productUpdateDTO.getStockQuantity());
            product.setImageUrl(productUpdateDTO.getImageUrl());
            product.setCategory(productUpdateDTO.getCategory());
            return productRepository.save(product);
        } else {
            throw new RuntimeException("Product with ID " + productUpdateDTO.getId() + " not found!");
        }
    }


    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
