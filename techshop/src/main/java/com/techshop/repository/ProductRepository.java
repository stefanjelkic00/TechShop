package com.techshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techshop.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // Ove metode već postoje u JpaRepository
}
