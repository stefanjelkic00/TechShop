package com.techshop.controller;

import com.techshop.dto.ProductDTO;
import com.techshop.dto.ProductDiscountDTO;
import com.techshop.dto.ProductUpdateDTO;
import com.techshop.model.Product;
import com.techshop.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
                product.getId(),
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
                product.getId(),
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
    public ResponseEntity<List<ProductDiscountDTO>> getProductsWithDiscount(
            @PathVariable Long userId,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        List<ProductDiscountDTO> discountedProducts = productService.getProductsWithDiscount(userId, query, category, sortBy, sortOrder, minPrice, maxPrice);
        return ResponseEntity.ok(discountedProducts);
    }
    
    @PostMapping("/filter")
    public ResponseEntity<List<ProductDTO>> filterProducts(@RequestBody ProductDTO productFilter) {
        List<ProductDTO> products = productService.getFilteredProducts(productFilter)
            .stream()
            .map(product -> new ProductDTO(
                product.getId(),
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
    public ResponseEntity<Product> createProduct(@RequestBody ProductDTO product) {
        Product savedProduct = productService.saveProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
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