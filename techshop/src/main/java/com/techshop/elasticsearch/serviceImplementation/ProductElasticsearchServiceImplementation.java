package com.techshop.elasticsearch.serviceImplementation;

import com.techshop.elasticsearch.model.ProductDocument;
import com.techshop.elasticsearch.repository.ProductElasticsearchRepository;
import com.techshop.elasticsearch.service.ProductElasticsearchService;

import org.springframework.stereotype.Service;
import java.util.stream.StreamSupport;
import java.util.regex.Pattern;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.ArrayList;


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
    
    @Override
    public List<ProductDocument> searchProducts(String query) {
        return repository.searchByNameOrDescription(query);
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
        text = Normalizer.normalize(text, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
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
        return repository.searchByNameOrDescription(query + "~"); // Elasticsearch fuzzy query
    }

    @Override
    public List<ProductDocument> searchWithSynonyms(String query) {
        List<String> expandedQueries = expandQueryWithSynonyms(query);
        return StreamSupport.stream(repository.findAll().spliterator(), false)
                .filter(product -> expandedQueries.stream().anyMatch(syn -> 
                        normalizeText(product.getName()).contains(syn) ||
                        normalizeText(product.getDescription()).contains(syn)))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> expandQueryWithSynonyms(String query) {
        Map<String, List<String>> synonyms = new HashMap<>();
        synonyms.put("tv", Arrays.asList("televizor", "lcd", "oled"));
        synonyms.put("mobilni", Arrays.asList("telefon", "smartphone", "android", "iphone"));
        synonyms.put("laptop", Arrays.asList("notebook", "macbook", "ultrabook"));
        synonyms.put("kamera", Arrays.asList("fotoaparat", "dslr", "mirrorless"));

        List<String> expandedQueries = new ArrayList<>();
        expandedQueries.add(normalizeText(query)); // Dodaj originalnu pretragu

        synonyms.forEach((key, values) -> {
            if (query.toLowerCase().contains(key)) {
                expandedQueries.addAll(values);
            }
        });

        return expandedQueries;
    }
    
    @Override
    public List<ProductDocument> searchAndSort(String query, String sortBy, String sortOrder) {
        List<ProductDocument> results = repository.searchByNameOrDescription(query);

        // Sortiranje po zadatom kriterijumu
        results.sort((p1, p2) -> {
            switch (sortBy) {
                case "price":
                    return sortOrder.equalsIgnoreCase("desc") 
                        ? p2.getPrice().compareTo(p1.getPrice()) 
                        : p1.getPrice().compareTo(p2.getPrice());

                case "date":
                    return sortOrder.equalsIgnoreCase("desc") 
                        ? p2.getCreatedAt().compareTo(p1.getCreatedAt()) 
                        : p1.getCreatedAt().compareTo(p2.getCreatedAt());

                default:
                    return 0;
            }
        });

        return results;
    }

    @Override
    public List<String> autocomplete(String query) {
        String normalizedQuery = normalizeText(query);
        
        return StreamSupport.stream(repository.findAll().spliterator(), false)
                .map(ProductDocument::getName)
                .filter(name -> normalizeText(name).startsWith(normalizedQuery))
                .limit(10) // Vraćamo do 10 sugestija
                .collect(Collectors.toList());
    }

    
    

}
