package com.techshop.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techshop.model.CartItem;
import com.techshop.model.Order;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

	static Optional<Order> findByUserId(Long userId) {
		// TODO Auto-generated method stub
		return null;
	}
}
