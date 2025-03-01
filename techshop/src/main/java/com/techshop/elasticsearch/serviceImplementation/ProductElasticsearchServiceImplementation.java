package com.techshop.elasticsearch.serviceImplementation;

import com.techshop.elasticsearch.model.ProductDocument;
import com.techshop.elasticsearch.repository.ProductElasticsearchRepository;
import com.techshop.elasticsearch.service.ProductElasticsearchService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductElasticsearchServiceImplementation implements ProductElasticsearchService {
    private final ProductElasticsearchRepository repository;

    public ProductElasticsearchServiceImplementation(ProductElasticsearchRepository repository) {
        this.repository = repository;
    }

    @Override
    public ProductDocument save(ProductDocument product) {
        return repository.save(product);
    }

    @Override
    public Iterable<ProductDocument> findAll() {
        return repository.findAll();
    }

    @Override
    public List<ProductDocument> findByName(String name) {
        return repository.findByName(name);
    }
}
