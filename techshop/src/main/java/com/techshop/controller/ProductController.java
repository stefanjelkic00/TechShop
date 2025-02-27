package com.techshop.controller;

import com.techshop.DTO.ProductDTO;
import com.techshop.DTO.ProductDiscountDTO;
import com.techshop.DTO.ProductUpdateDTO;
import com.techshop.model.Product;
import com.techshop.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
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


    @GetMapping("/discounted/{userId}")
    public ResponseEntity<List<ProductDiscountDTO>> getProductsWithDiscount(@PathVariable Long userId) {
        return ResponseEntity.ok(productService.getProductsWithDiscount(userId));
    }
    

}
