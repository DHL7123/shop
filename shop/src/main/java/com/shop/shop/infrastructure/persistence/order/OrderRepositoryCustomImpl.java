package com.shop.shop.infrastructure.persistence.order;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shop.shop.infrastructure.constant.OrderStatus;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

import static com.shop.shop.infrastructure.persistence.order.QOrders.orders;

@Repository
public class OrderRepositoryCustomImpl implements OrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public OrderRepositoryCustomImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<Orders> findOrdersWithConditions(Long customerId, LocalDate startDate, LocalDate endDate, OrderStatus status) {
        BooleanBuilder builder = new BooleanBuilder();

        // 필수 조건: 고객 ID
        builder.and(orders.customer.id.eq(customerId));

        // 선택적 조건: 날짜
        if (startDate != null && endDate != null) {
            builder.and(orders.orderDate.between(startDate.atStartOfDay(), endDate.atTime(23, 59, 59)));
        }

        // 선택적 조건: 주문 상태
        if (status != null) {
            builder.and(orders.status.eq(status));
        }

        return queryFactory.selectFrom(orders)
                .where(builder)
                .fetch();
    }
}
