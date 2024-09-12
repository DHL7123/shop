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

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private String orderNumber;

    @Column(nullable = false)
    private LocalDateTime orderDate;

    @Column(nullable = false)
    private String status; // ORDER, SHIPPING, 완료, CANCEL 등

    @Column(nullable = false)
    private String paymentMethod;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String zipCode;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private Long quantity; // 수량은 0보다 커야 함

    @Column(nullable = false)
    private Long totalPrice; // 총 가격은 0보다 커야 함

    @Column
    private String remarks;

    @JsonIgnore
    @OneToMany(mappedBy = "customer")
    private List<Orders> orders;

    // 주문 생성 메서드
    public static Orders create(Long productId, Long quantity, Customer customer) {
        validateOrder(productId, quantity);
        return new Orders(productId, quantity, customer);
    }

    // 생성자
    public Orders(Long productId, Long quantity, Customer customer) {
        validateOrder(productId, quantity);
        this.productId = productId;
        this.quantity = quantity;
        this.customer = customer;
        this.orderDate = LocalDateTime.now();
        this.status = "PENDING";
    }

    // 유효성 검사
    private static void validateOrder(Long productId, Long quantity) {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 제품 ID입니다.");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("수량은 0보다 커야 합니다.");
        }
    }

}
