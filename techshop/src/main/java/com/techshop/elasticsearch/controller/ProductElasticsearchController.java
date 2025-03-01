package com.techshop.elasticsearch.controller;
import org.springframework.web.bind.annotation.*;

import com.techshop.elasticsearch.model.ProductDocument;
import com.techshop.elasticsearch.service.ProductElasticsearchService;

import java.util.List;

@RestController
@RequestMapping("/api/products/elasticsearch")
public class ProductElasticsearchController {
    private final ProductElasticsearchService service;

    public ProductElasticsearchController(ProductElasticsearchService service) {
        this.service = service;
    }

    @PostMapping
    public ProductDocument save(@RequestBody ProductDocument product) {
        return service.save(product);
    }

    @GetMapping
    public Iterable<ProductDocument> findAll() {
        return service.findAll();
    }

    @GetMapping("/search")
    public List<ProductDocument> searchByName(@RequestParam String name) {
        return service.findByName(name);
    }
}
