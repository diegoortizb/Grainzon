package com.grainzon.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.grainzon.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {
}
