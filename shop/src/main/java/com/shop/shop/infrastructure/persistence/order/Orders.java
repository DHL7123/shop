package com.shop.shop.infrastructure.persistence.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.shop.shop.infrastructure.persistence.member.Customer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
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
    @Column
    private String status; // ORDER, SHIPPING, 완료, CANCEL, ..
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
    private Long quantity; // nullable,, int 로 가는게 맞음
    @Column
    private Long totalPrice;
    @Column
    private String remarks;

    @JsonIgnore
    @OneToMany(mappedBy = "customer")
    private List<Orders> orders; // 순환 참조 방지

    public static Orders create(Long productId, Long quantity, Customer customer) {
        return new Orders(productId, quantity, customer);
    }
    public Orders(Long productId, Long quantity, Customer customer) {
        this.productId = productId;
        this.quantity = quantity;
        this.customer = customer;  // Customer 엔티티 할당
        this.orderDate = LocalDateTime.now();  // 주문 날짜 초기화
        this.status = "PENDING";  // 기본 상태 설정
    }

    public void updateOrder(Long productId, Long quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }
    public void cancel() {
        this.status = "CANCEL";
    }
}