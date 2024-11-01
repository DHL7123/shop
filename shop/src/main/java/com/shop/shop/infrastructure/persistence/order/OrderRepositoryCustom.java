package com.shop.shop.infrastructure.persistence.order;

import com.shop.shop.infrastructure.constant.OrderStatus;

import java.time.LocalDate;
import java.util.List;

public interface OrderRepositoryCustom {
    List<Orders> findOrdersWithConditions(String customerId, LocalDate startDate, LocalDate endDate, OrderStatus status);
}
