package com.shop.shop.infrastructure.persistence.product;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository <Product, Long>, CustomProductRepository{
    Optional<Product> findById(Long id);

}

