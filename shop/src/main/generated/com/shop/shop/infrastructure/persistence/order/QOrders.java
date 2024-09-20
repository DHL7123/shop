package com.shop.shop.infrastructure.persistence.order;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOrders is a Querydsl query type for Orders
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOrders extends EntityPathBase<Orders> {

    private static final long serialVersionUID = 1498216530L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QOrders orders = new QOrders("orders");

    public final StringPath address = createString("address");

    public final com.shop.shop.infrastructure.persistence.member.QCustomer customer;

    public final StringPath name = createString("name");

    public final DateTimePath<java.time.LocalDateTime> orderDate = createDateTime("orderDate", java.time.LocalDateTime.class);

    public final ListPath<Orders, QOrders> orderList = this.<Orders, QOrders>createList("orderList", Orders.class, QOrders.class, PathInits.DIRECT2);

    public final StringPath orderNumber = createString("orderNumber");

    public final StringPath paymentMethod = createString("paymentMethod");

    public final StringPath phone = createString("phone");

    public final NumberPath<Long> pk = createNumber("pk", Long.class);

    public final NumberPath<Long> productId = createNumber("productId", Long.class);

    public final NumberPath<Long> quantity = createNumber("quantity", Long.class);

    public final StringPath remarks = createString("remarks");

    public final EnumPath<com.shop.shop.infrastructure.constant.OrderStatus> status = createEnum("status", com.shop.shop.infrastructure.constant.OrderStatus.class);

    public final NumberPath<Long> totalPrice = createNumber("totalPrice", Long.class);

    public final StringPath zipCode = createString("zipCode");

    public QOrders(String variable) {
        this(Orders.class, forVariable(variable), INITS);
    }

    public QOrders(Path<? extends Orders> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QOrders(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QOrders(PathMetadata metadata, PathInits inits) {
        this(Orders.class, metadata, inits);
    }

    public QOrders(Class<? extends Orders> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.customer = inits.isInitialized("customer") ? new com.shop.shop.infrastructure.persistence.member.QCustomer(forProperty("customer")) : null;
    }

}

