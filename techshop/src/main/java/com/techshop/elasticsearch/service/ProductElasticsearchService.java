package com.techshop.elasticsearch.service;

import com.techshop.elasticsearch.model.ProductDocument;

import java.util.List;

public interface ProductElasticsearchService {
    ProductDocument save(ProductDocument product);
    Iterable<ProductDocument> findAll();
    List<ProductDocument> findByName(String name);
    
    // Napredna pretraga (full-text + fuzzy search)
    List<ProductDocument> searchProducts(String query);

    // Pretraga uz normalizaciju (ćirilica/latinica + specijalna slova)
    List<ProductDocument> searchWithNormalization(String query);
    
    
    // Metoda za normalizaciju teksta
    String normalizeText(String text);
    
    // Metode za pretragu teksta upisanu greskama
    List<ProductDocument> fuzzySearch(String query);
    List<ProductDocument> searchWithSynonyms(String query);
    List<String> expandQueryWithSynonyms(String query);
    
    //Sortiranje po ceni ili datumu 
    List<ProductDocument> searchAndSort(String query, String sortBy, String sortOrder);
    
    //prikaz sugestija pri kucanju
    List<String> autocomplete(String query);



}
