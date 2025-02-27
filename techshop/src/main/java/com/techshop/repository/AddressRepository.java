package com.techshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techshop.model.*;

public interface AddressRepository extends JpaRepository<Address, Long> {
}
