package com.techshop.elasticsearch.controller;

import com.techshop.elasticsearch.model.ProductDocument;
import com.techshop.elasticsearch.service.ProductElasticsearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/elasticsearch/products")
public class ProductElasticsearchController {

    private final ProductElasticsearchService service;

    @Autowired
    public ProductElasticsearchController(ProductElasticsearchService service) {
        this.service = service;
    }

    // 📌 Sačuvaj proizvod u Elasticsearch
    @PostMapping("/save")
    public ProductDocument save(@RequestBody ProductDocument product) {
        return service.save(product);
    }

    // 📌 Prikaz svih proizvoda iz Elasticsearch indeksa
    @GetMapping("/all")
    public Iterable<ProductDocument> findAll() {
        return service.findAll();
    }

    // 📌 Full-text pretraga (fuzzy + ćirilica/latinica)
    @GetMapping("/search")
    public List<ProductDocument> searchProducts(@RequestParam String query) {
        return service.searchProducts(query);
    }

    // 📌 Pretraga uz normalizaciju ćirilice/latinice + specijalnih slova
    @GetMapping("/search-normalized")
    public List<ProductDocument> searchWithNormalization(@RequestParam String query) {
        return service.searchWithNormalization(query);
    }

    // 📌 Fuzzy search (pronalazi rezultate čak i ako korisnik pogreši u kucanju)
    @GetMapping("/search-fuzzy")
    public List<ProductDocument> fuzzySearch(@RequestParam String query) {
        return service.fuzzySearch(query);
    }

    // 📌 Sortiranje rezultata (po ceni ili datumu)
    @GetMapping("/search-sort")
    public List<ProductDocument> searchAndSort(
            @RequestParam String query,
            @RequestParam(defaultValue = "price") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder) {
        return service.searchAndSort(query, sortBy, sortOrder);
    }

    // 📌 Autocomplete (prikaz sugestija pri kucanju)
    @GetMapping("/autocomplete")
    public List<String> autocomplete(@RequestParam String query) {
        return service.autocomplete(query);
    }
    
    @GetMapping("/filter")
    public ResponseEntity<List<ProductDocument>> filterProducts(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Float minPrice,
            @RequestParam(required = false) Float maxPrice,
            @RequestParam(defaultValue = "price_asc") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder) {
        List<ProductDocument> products = service.filterProducts(category, minPrice, maxPrice, sortBy, sortOrder, query);
        return ResponseEntity.ok(products);
    }
}