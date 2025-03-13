package com.techshop.elasticsearch.serviceImplementation;

import com.techshop.elasticsearch.model.ProductDocument;
import com.techshop.elasticsearch.repository.ProductElasticsearchRepository;
import com.techshop.elasticsearch.service.ProductElasticsearchService;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.Collections;

@Service
public class ProductElasticsearchServiceImplementation implements ProductElasticsearchService {
    private final ProductElasticsearchRepository repository;
    private final ElasticsearchOperations elasticsearchOperations;

    public ProductElasticsearchServiceImplementation(ProductElasticsearchRepository repository, ElasticsearchOperations elasticsearchOperations) {
        this.repository = repository;
        this.elasticsearchOperations = elasticsearchOperations;
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

    @Override
    public List<ProductDocument> searchProducts(String query) {
        String normalizedQuery = normalizeText(query);
        Criteria criteria = new Criteria("name").expression("*" + normalizedQuery + "*")
                .or(new Criteria("description").expression("*" + normalizedQuery + "*"));
        CriteriaQuery criteriaQuery = new CriteriaQuery(criteria);

        return elasticsearchOperations.search(criteriaQuery, ProductDocument.class)
                .stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDocument> searchWithNormalization(String query) {
        String normalizedQuery = normalizeText(query);
        return StreamSupport.stream(repository.findAll().spliterator(), false)
                .filter(product -> normalizeText(product.getName()).contains(normalizedQuery) ||
                                   normalizeText(product.getDescription()).contains(normalizedQuery))
                .collect(Collectors.toList());
    }

    @Override
    public String normalizeText(String text) {
        if (text == null) return "";
        text = java.text.Normalizer.normalize(text, java.text.Normalizer.Form.NFD);
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        text = pattern.matcher(text).replaceAll("");

        // Konverzija ćirilice u latinicu
        text = text.replace("Љ", "Lj").replace("љ", "lj")
                   .replace("Њ", "Nj").replace("њ", "nj")
                   .replace("Џ", "Dž").replace("џ", "dž")
                   .replace("Ђ", "Dj").replace("ђ", "dj")
                   .replace("Ч", "C").replace("ч", "c")
                   .replace("Ć", "C").replace("ć", "c")
                   .replace("Š", "S").replace("š", "s")
                   .replace("Đ", "D").replace("đ", "d")
                   .replace("Ž", "Z").replace("ž", "z");

        return text.toLowerCase();
    }

    @Override
    public List<ProductDocument> fuzzySearch(String query) {
        String normalizedQuery = normalizeText(query);
        Criteria criteria = new Criteria("name").fuzzy(normalizedQuery)
                .or(new Criteria("description").fuzzy(normalizedQuery));
        CriteriaQuery criteriaQuery = new CriteriaQuery(criteria);

        return elasticsearchOperations.search(criteriaQuery, ProductDocument.class)
                .stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDocument> searchAndSort(String query, String sortBy, String sortOrder) {
        String normalizedQuery = normalizeText(query);
        Criteria criteria = new Criteria("name").expression("*" + normalizedQuery + "*")
                .or(new Criteria("description").expression("*" + normalizedQuery + "*"));
        CriteriaQuery criteriaQuery = new CriteriaQuery(criteria);

        if (sortBy != null && sortOrder != null) {
            String adjustedSortBy = sortBy;
            if (sortBy.contains("_")) {
                adjustedSortBy = sortBy.split("_")[0];
            }
            if (adjustedSortBy.equals("price") || adjustedSortBy.equals("createdAt")) {
                criteriaQuery.addSort(Sort.by(Sort.Order.by(adjustedSortBy).with(Sort.Direction.fromString(sortOrder))));
            }
        }

        return elasticsearchOperations.search(criteriaQuery, ProductDocument.class)
                .stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> autocomplete(String query) {
        String normalizedQuery = normalizeText(query);
        return StreamSupport.stream(repository.findAll().spliterator(), false)
                .map(ProductDocument::getName)
                .filter(name -> normalizeText(name).startsWith(normalizedQuery))
                .limit(10)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDocument> filterProducts(String category, Float minPrice, Float maxPrice, String sortBy, String sortOrder, String query) {
        System.out.println("🔹 Filtering products with params: category=" + category + ", minPrice=" + minPrice + ", maxPrice=" + maxPrice + 
                           ", sortBy=" + sortBy + ", sortOrder=" + sortOrder + ", query=" + query);

        Criteria criteria = new Criteria();

        if (category != null && !category.isEmpty()) {
            System.out.println("🔹 Adding category filter: " + category);
            criteria.and(Criteria.where("category").is(category));
        }

        if (minPrice != null) {
            System.out.println("🔹 Adding minPrice filter: " + minPrice);
            criteria.and(Criteria.where("price").greaterThanEqual(minPrice));
        }

        if (maxPrice != null) {
            System.out.println("🔹 Adding maxPrice filter: " + maxPrice);
            criteria.and(Criteria.where("price").lessThanEqual(maxPrice));
        }

        if (query != null && !query.isEmpty()) {
            System.out.println("🔹 Adding query filter: " + query);
            String normalizedQuery = normalizeText(query);
            criteria.and(new Criteria("name").expression("*" + normalizedQuery + "*"));
        }

        CriteriaQuery criteriaQuery = new CriteriaQuery(criteria);

        // Dodavanje sortiranja sa proverom
        if (sortBy != null && sortOrder != null) {
            String adjustedSortBy = sortBy;
            if (sortBy.contains("_")) {
                adjustedSortBy = sortBy.split("_")[0]; // Uzima samo "price"
            }
            if (adjustedSortBy.equals("price") || adjustedSortBy.equals("createdAt")) {
                System.out.println("🔹 Adding sort: " + adjustedSortBy + " " + sortOrder);
                criteriaQuery.addSort(Sort.by(Sort.Order.by(adjustedSortBy).with(Sort.Direction.fromString(sortOrder))));
            } else {
                System.out.println("⚠️ Invalid sort field: " + adjustedSortBy + ", skipping sort.");
            }
        }

        System.out.println("🔹 Created CriteriaQuery: " + criteriaQuery);

        List<ProductDocument> products = elasticsearchOperations.search(criteriaQuery, ProductDocument.class)
                .stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        System.out.println("🔹 Found " + products.size() + " products: " + products);
        return products;
    }
}