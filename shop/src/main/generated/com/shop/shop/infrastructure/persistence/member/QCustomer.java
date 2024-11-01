package com.shop.shop.infrastructure.persistence.member;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCustomer is a Querydsl query type for Customer
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCustomer extends EntityPathBase<Customer> {

    private static final long serialVersionUID = -475611387L;

    public static final QCustomer customer = new QCustomer("customer");

    public final StringPath accountCode = createString("accountCode");

    public final StringPath address = createString("address");

    public final StringPath city = createString("city");

    public final StringPath country = createString("country");

    public final StringPath customerId = createString("customerId");

    public final StringPath email = createString("email");

    public final DateTimePath<java.time.LocalDateTime> lastLoginDate = createDateTime("lastLoginDate", java.time.LocalDateTime.class);

    public final ListPath<com.shop.shop.infrastructure.persistence.order.Orders, com.shop.shop.infrastructure.persistence.order.QOrders> orders = this.<com.shop.shop.infrastructure.persistence.order.Orders, com.shop.shop.infrastructure.persistence.order.QOrders>createList("orders", com.shop.shop.infrastructure.persistence.order.Orders.class, com.shop.shop.infrastructure.persistence.order.QOrders.class, PathInits.DIRECT2);

    public final StringPath password = createString("password");

    public final StringPath phoneNumber = createString("phoneNumber");

    public final NumberPath<Long> pk = createNumber("pk", Long.class);

    public final StringPath remarks = createString("remarks");

    public final DateTimePath<java.time.LocalDateTime> signupDate = createDateTime("signupDate", java.time.LocalDateTime.class);

    public final StringPath state = createString("state");

    public final StringPath status = createString("status");

    public final DateTimePath<java.time.LocalDateTime> statusChangedDate = createDateTime("statusChangedDate", java.time.LocalDateTime.class);

    public final StringPath userName = createString("userName");

    public final StringPath zipCode = createString("zipCode");

    public QCustomer(String variable) {
        super(Customer.class, forVariable(variable));
    }

    public QCustomer(Path<? extends Customer> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCustomer(PathMetadata metadata) {
        super(Customer.class, metadata);
    }

}

