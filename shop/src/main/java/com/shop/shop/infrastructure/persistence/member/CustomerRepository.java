package com.shop.shop.infrastructure.persistence.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Optional<Customer> findByCustomerIdAndStatus(String customerId, String status);
    Optional<Customer> findByCustomerId(String customerId);
    boolean existsByCustomerId(String customerId);
    boolean existsByEmail(String email);
}
