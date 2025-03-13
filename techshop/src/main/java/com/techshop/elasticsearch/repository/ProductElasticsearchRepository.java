package com.techshop.elasticsearch.repository;

import java.util.List;

import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import com.techshop.elasticsearch.model.ProductDocument;

@Repository
public interface ProductElasticsearchRepository extends ElasticsearchRepository<ProductDocument, String> {

    List<ProductDocument> findByName(String name);

    // Uklanjamo staru metodu i oslanjamo se na CriteriaQuery u servisu
    // Ako želiš zadržati, možeš koristiti @Query
    @Query("{\"bool\": {\"should\": [{\"wildcard\": {\"name\": \"*?0*\"}}, {\"wildcard\": {\"description\": \"*?0*\"}}]}}")
    List<ProductDocument> searchByNameOrDescription(String query);

    List<ProductDocument> findAll();
}