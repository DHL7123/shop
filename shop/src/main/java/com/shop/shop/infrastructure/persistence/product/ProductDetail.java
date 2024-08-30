package com.shop.shop.infrastructure.persistence.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer pk; // primary key
    @OneToOne
    @JoinColumn(name = "product_id")
    private Product product;
    @Column
    private String description;
    @Column
    private LocalDateTime registeredDate;
    @Column
    private Integer soldout;
    @Column
    private Integer viewCount;

}
