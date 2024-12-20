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
    private Long pk; // primary key
    @OneToOne
    @JoinColumn(name = "product_id")
    private Product product;
    @Column
    private LocalDateTime registeredDate;
    @Column
    private Long viewCount;

}
