package com.techshop.elasticsearch.repository;
import java.util.List;

import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import com.techshop.elasticsearch.model.ProductDocument;

@Repository
public interface ProductElasticsearchRepository extends ElasticsearchRepository<ProductDocument, String> {

	List<ProductDocument> findByName(String name);

    @Query("{ \"multi_match\": { \"query\": \"?0\", \"fields\": [\"name\", \"description\"] } }")
    List<ProductDocument> searchByNameOrDescription(String query);
}
