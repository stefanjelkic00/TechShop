package com.techshop.controller;

import com.techshop.dto.ProductDTO;
import com.techshop.dto.ProductDiscountDTO;
import com.techshop.dto.ProductUpdateDTO;
import com.techshop.enums.Category;
import com.techshop.model.Product;
import com.techshop.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<ProductDTO> products = productService.getAllProducts()
            .stream()
            .map(product -> new ProductDTO(
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getImageUrl(),
                product.getCategory()
            ))
            .collect(Collectors.toList());
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
            .map(product -> new ProductDTO(
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getImageUrl(),
                product.getCategory()
            ))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/discounted/{userId}")
    public ResponseEntity<List<ProductDiscountDTO>> getProductsWithDiscount(@PathVariable Long userId) {
        return ResponseEntity.ok(productService.getProductsWithDiscount(userId));
    }
    
    
    @PostMapping("/filter")
    public ResponseEntity<List<ProductDTO>> filterProducts(@RequestBody ProductDTO productFilter) {
        List<ProductDTO> products = productService.getFilteredProducts(productFilter)
            .stream()
            .map(product -> new ProductDTO(
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getImageUrl(),
                product.getCategory()
            ))
            .collect(Collectors.toList());

        return ResponseEntity.ok(products);
    }
    
    
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        return ResponseEntity.ok(productService.getAllCategories());
    }




    @PostMapping
    public Product createProduct(@RequestBody ProductDTO product) {
        return productService.saveProduct(product);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody ProductUpdateDTO productUpdateDTO) {
        if (!id.equals(productUpdateDTO.getId())) {
            return ResponseEntity.badRequest().build();
        }
        Product updatedProduct = productService.updateProduct(productUpdateDTO);
        return ResponseEntity.ok(updatedProduct);
    }
    

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }




}
