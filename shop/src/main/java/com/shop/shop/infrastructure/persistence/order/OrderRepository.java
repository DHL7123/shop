package com.shop.shop.infrastructure.persistence.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Long>, OrderRepositoryCustom {

    @Query("SELECT o FROM Orders o JOIN FETCH o.customer")
    List<Orders> findAllWithCustomer();


}
