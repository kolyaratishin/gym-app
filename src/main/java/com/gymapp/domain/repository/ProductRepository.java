package com.gymapp.domain.repository;

import com.gymapp.domain.product.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(Long id);

    List<Product> findAll();

    List<Product> findActive();

    List<Product> findLowStock(int threshold);

    void update(Product product);
}
