package com.shop.shop.infrastructure.persistence.order;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shop.shop.infrastructure.constant.OrderStatus;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;


@Repository
public class OrderRepositoryCustomImpl implements OrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public OrderRepositoryCustomImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public List<Orders> findOrdersWithConditions(String customerId, LocalDate startDate, LocalDate endDate, OrderStatus status) {
        QOrders qOrder = QOrders.orders;

        BooleanBuilder builder = new BooleanBuilder();

        // 필수 조건: 고객 ID
        builder.and(qOrder.customer.customerId.eq(customerId));

        // 선택적 조건: 날짜
        if (startDate != null && endDate != null) {
            builder.and(qOrder.orderDate.between(startDate.atStartOfDay(), endDate.atTime(23, 59, 59)));
        }

        // 선택적 조건: 주문 상태
        if (status != null) {
            builder.and(qOrder.status.eq(status));
        }

        return queryFactory.selectFrom(qOrder)
                .where(builder)
                .fetch();
    }
}
