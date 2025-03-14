package com.techshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.techshop.model.Order;
import com.techshop.model.User;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
	
    long countByUser(User user);

}
