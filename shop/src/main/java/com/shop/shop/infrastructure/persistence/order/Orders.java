package com.shop.shop.infrastructure.persistence.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.shop.shop.infrastructure.constant.OrderStatus;
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

    @Enumerated(EnumType.STRING)  // 문자열로 저장
    private OrderStatus status; // ORDER, SHIPPING, 완료, CANCEL 등

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
    private Long quantity; // 수량은 0보다 커야 함

    @Column
    private Long totalPrice; // 총 가격은 0보다 커야 함

    @Column
    private String remarks;

    @JsonIgnore
    @OneToMany(mappedBy = "customer")
    private List<Orders> orders;

    // 주문 생성 메서드
    public static Orders create(Long productId, Long quantity, Customer customer) {
        validateOrder(productId, quantity);
        Orders order = new Orders(productId, quantity, customer);
        order.setStatus(OrderStatus.PENDING);
        return order;
    }

    // 생성자
    public Orders(Long productId, Long quantity, Customer customer) {
        validateOrder(productId, quantity);
        this.productId = productId;
        this.quantity = quantity;
        this.customer = customer;
        this.orderDate = LocalDateTime.now();
        this.status = OrderStatus.PENDING;
    }

    // 주문 상태 변경 메서드
    public void changeStatus(OrderStatus newStatus) {
        this.status = newStatus;
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
