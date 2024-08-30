package com.shop.shop.infrastructure.persistence.order;

import com.shop.shop.infrastructure.persistence.member.Customer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Orders {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer pk;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_pk")
    private Customer customer;
    @Column
    private Integer productId;
    @Column
    private String orderNumber;
    @Column
    private LocalDateTime orderDate;
    @Column
    private String status;
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
    private Integer quantity;
    @Column
    private BigDecimal totalPrice;
    @Column
    private String remarks;
}