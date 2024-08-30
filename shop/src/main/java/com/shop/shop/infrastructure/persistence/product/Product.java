package com.shop.shop.infrastructure.persistence.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer pk; // primary key
    @Column
    private String name;
    @Column
    private String imageUrl;
    @Column
    private Integer stockQuantity;
    @Column
    private String category;
    @Column
    private Integer price;
    @Column
    private String shipping;
}
