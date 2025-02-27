package com.techshop.serviceImplementation;

import com.techshop.DTO.ProductDTO;
import com.techshop.DTO.ProductDiscountDTO;
import com.techshop.DTO.ProductUpdateDTO;
import com.techshop.model.Product;
import com.techshop.model.User;
import com.techshop.repository.ProductRepository;
import com.techshop.repository.UserRepository;
import com.techshop.service.ProductService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductServiceImplementation implements ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    
    public ProductServiceImplementation(UserRepository userRepository,ProductRepository productRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository; // Dodato!

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
        Product product = new Product();
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setStockQuantity(productDTO.getStockQuantity());
        product.setImageUrl(productDTO.getImageUrl());
        product.setCategory(productDTO.getCategory());

        return productRepository.save(product);
    }
    
    @Override
    public Product updateProduct(ProductUpdateDTO productUpdateDTO) {
        Optional<Product> existingProduct = productRepository.findById(productUpdateDTO.getId());

        if (existingProduct.isPresent()) {
            Product product = existingProduct.get();
            product.setName(productUpdateDTO.getName());
            product.setDescription(productUpdateDTO.getDescription());
            product.setPrice(productUpdateDTO.getPrice());
            product.setStockQuantity(productUpdateDTO.getStockQuantity());
            product.setImageUrl(productUpdateDTO.getImageUrl());
            product.setCategory(productUpdateDTO.getCategory());
            return productRepository.save(product);
        } else {
            throw new RuntimeException("Product with ID " + productUpdateDTO.getId() + " not found!");
        }
    }


    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
    
    
    @Override
    public List<ProductDiscountDTO> getProductsWithDiscount(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        List<Product> products = productRepository.findAll();

        return products.stream().map(product -> {
            BigDecimal discount = getDiscountForUser(user);
            BigDecimal discountedPrice = product.getPrice().subtract(product.getPrice().multiply(discount));

            return new ProductDiscountDTO(product.getId(), product.getName(), product.getDescription(),
                    product.getPrice(), discountedPrice, product.getImageUrl(), product.getCategory());
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

    
    
}
