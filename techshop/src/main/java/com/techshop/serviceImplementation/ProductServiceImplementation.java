package com.techshop.serviceImplementation;

import com.techshop.dto.ProductDTO;
import com.techshop.dto.ProductDiscountDTO;
import com.techshop.dto.ProductUpdateDTO;
import com.techshop.elasticsearch.model.ProductDocument;
import com.techshop.elasticsearch.repository.ProductElasticsearchRepository;
import com.techshop.elasticsearch.service.ProductSyncService;
import com.techshop.enums.Category;
import com.techshop.model.Product;
import com.techshop.model.User;
import com.techshop.repository.ProductRepository;
import com.techshop.repository.UserRepository;
import com.techshop.service.ProductService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductServiceImplementation implements ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductSyncService productSyncService;
    private final ProductElasticsearchRepository productElasticsearchRepository;

    @Autowired
    public ProductServiceImplementation(
        UserRepository userRepository, 
        ProductRepository productRepository,
        ProductElasticsearchRepository productElasticsearchRepository,
        ProductSyncService productSyncService
    ) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.productElasticsearchRepository = productElasticsearchRepository;
        this.productSyncService = productSyncService;
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public Product saveProduct(ProductDTO productDTO) {
        if (productDTO.getStockQuantity() < 0) {
            throw new RuntimeException("Stock quantity cannot be negative.");
        }

        Product product = new Product();
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setStockQuantity(productDTO.getStockQuantity());
        product.setImageUrl(productDTO.getImageUrl());
        product.setCategory(productDTO.getCategory());

        // 1️⃣ Sačuvaj u MySQL
        Product savedProduct = productRepository.save(product);

        // 2️⃣ Konvertuj u Elasticsearch dokument
        ProductDocument productDocument = productSyncService.convertToDocument(savedProduct);

        // 3️⃣ Sačuvaj u Elasticsearch
        productElasticsearchRepository.save(productDocument);

        return savedProduct;
    }

    @Override
    public Product updateProduct(ProductUpdateDTO productUpdateDTO) {
        Optional<Product> existingProduct = productRepository.findById(productUpdateDTO.getId());

        if (existingProduct.isPresent()) {
            Product product = existingProduct.get();

            if (productUpdateDTO.getStockQuantity() < 0) {
                throw new RuntimeException("Stock quantity cannot be negative.");
            }

            product.setName(productUpdateDTO.getName());
            product.setDescription(productUpdateDTO.getDescription());
            product.setPrice(productUpdateDTO.getPrice());
            product.setStockQuantity(productUpdateDTO.getStockQuantity());
            product.setImageUrl(productUpdateDTO.getImageUrl());
            product.setCategory(productUpdateDTO.getCategory());

            Product updatedProduct = productRepository.save(product);

            // Sinhronizacija sa Elasticsearch-om
            ProductDocument productDocument = productSyncService.convertToDocument(updatedProduct);
            productElasticsearchRepository.save(productDocument);

            return updatedProduct;
        } else {
            throw new RuntimeException("Product with ID " + productUpdateDTO.getId() + " not found!");
        }
    }

    @Override
    public void deleteProduct(Long id) {
        Optional<Product> existingProduct = productRepository.findById(id);

        if (existingProduct.isPresent()) {
            productRepository.deleteById(id);

            // Brisanje iz Elasticsearch-a
            productElasticsearchRepository.deleteById(id.toString());
        } else {
            throw new RuntimeException("Product with ID " + id + " not found!");
        }
    }

    @Override
    public List<ProductDiscountDTO> getProductsWithDiscount(Long userId, String searchQuery, String category, String sortBy, String sortOrder, BigDecimal minPrice, BigDecimal maxPrice) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        List<Product> products = productRepository.findAll();

        // Filtriranje
        if (searchQuery != null && !searchQuery.isEmpty()) {
            products = products.stream()
                .filter(p -> p.getName().toLowerCase().contains(searchQuery.toLowerCase()))
                .collect(Collectors.toList());
        }
        if (category != null && !category.isEmpty()) {
            products = products.stream()
                .filter(p -> p.getCategory().name().equalsIgnoreCase(category))
                .collect(Collectors.toList());
        }
        if (minPrice != null) {
            products = products.stream()
                .filter(p -> p.getPrice().compareTo(minPrice) >= 0)
                .collect(Collectors.toList());
        }
        if (maxPrice != null) {
            products = products.stream()
                .filter(p -> p.getPrice().compareTo(maxPrice) <= 0)
                .collect(Collectors.toList());
        }

        // Sortiranje
        if (sortBy != null && !sortBy.isEmpty()) {
            Comparator<Product> comparator;
            switch (sortBy.toLowerCase()) {
                case "price":
                    comparator = Comparator.comparing(Product::getPrice);
                    break;
                case "name":
                    comparator = Comparator.comparing(Product::getName);
                    break;
                default:
                    comparator = Comparator.comparing(Product::getId);
            }
            if ("desc".equalsIgnoreCase(sortOrder)) {
                comparator = comparator.reversed();
            }
            products.sort(comparator);
        }

        // Primena popusta i konverzija u DTO
        BigDecimal discount = getDiscountForUser(user);
        return products.stream().map(product -> {
            BigDecimal discountedPrice = product.getPrice().subtract(product.getPrice().multiply(discount));
            return new ProductDiscountDTO(product.getId(), product.getName(), product.getDescription(),
                    product.getPrice(), discountedPrice, product.getImageUrl(), product.getCategory(), product.getStockQuantity());
        }).collect(Collectors.toList());
    }

    private BigDecimal getDiscountForUser(User user) {
        switch (user.getCustomerType()) {
            case VIP:
                return BigDecimal.valueOf(0.30); // 30% popust
            case PLATINUM:
                return BigDecimal.valueOf(0.20); // 20% popust
            case PREMIUM:
                return BigDecimal.valueOf(0.10); // 10% popust
            default:
                return BigDecimal.ZERO; // Nema popusta za REGULAR korisnike
        }
    }

    @Override
    public List<Product> getFilteredProducts(ProductDTO productFilter) {
        // Početni upit - svi proizvodi
        List<Product> products = productRepository.findAll();

        // Filtracija po kategoriji ako je zadato
        if (productFilter.getCategory() != null) {
            products = products.stream()
                .filter(product -> product.getCategory() == productFilter.getCategory())
                .collect(Collectors.toList());
        }

        // Filtracija po nazivu ako je zadato
        if (productFilter.getName() != null && !productFilter.getName().isEmpty()) {
            products = products.stream()
                .filter(product -> product.getName().toLowerCase().contains(productFilter.getName().toLowerCase()))
                .collect(Collectors.toList());
        }

        // Filtracija po minimalnoj ceni ako je zadato
        if (productFilter.getPrice() != null) {
            products = products.stream()
                .filter(product -> product.getPrice().compareTo(productFilter.getPrice()) >= 0)
                .collect(Collectors.toList());
        }

        return products;
    }
    
    @Override
    public List<String> getAllCategories() {
        return Arrays.stream(Category.values())
                     .map(Enum::name)
                     .collect(Collectors.toList());
    }
}