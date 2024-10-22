package com.shop.shop.infrastructure.persistence.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.shop.shop.infrastructure.constant.OrderStatus;
import com.shop.shop.infrastructure.persistence.member.Customer;
import com.shop.shop.infrastructure.persistence.product.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_pk")
    private Customer customer;

    @Column
    private Long productId;

    @Column
    private String orderNumber;

    @Column
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column
    private String paymentMethod;

    @Column
    private String name;

    @Column
    private String zipCode;

    @Column
    private String address;

    @Column
    private String phone;

    @Column
    private Long quantity;

    @Column
    private Long totalPrice;

    @Column
    private String remarks;

    @JsonIgnore
    @OneToMany(mappedBy = "customer")
    private List<Orders> orderList;

    public Orders(Product product, Long quantity, Customer customer, OrderStatus orderStatus, LocalDateTime now) {
        this.productId = product.getId();  // 상품 ID 설정
        this.quantity = quantity;          // 주문 수량 설정
        this.customer = customer;          // 고객 정보 설정
        this.status = orderStatus;         // 주문 상태 설정
        this.orderDate = now;              // 주문 날짜 설정
    }
}

