package com.techshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techshop.model.Cart;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
}
