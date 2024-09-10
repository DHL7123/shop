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
    private Long pk; // primary key
    @Column
    private String name;
    @Column
    private String imageUrl;
    @Column
    private Long stockQuantity;
    @Column
    private Integer soldout;
    @Column
    private String category;
    @Column
    private Long price;
    @Column
    private String shipping;

    // 재고 감소
    public void decreaseStockQuantity(Long StockQuantity) {
        if(this.stockQuantity < StockQuantity){
            throw new IllegalArgumentException("Stock quantity should be greater than stock quantity");
        }
        this.stockQuantity -= StockQuantity;
    }
    // 재고 증가
    public void increaseStock(Long StockQuantity) {
        this.stockQuantity += StockQuantity;
    }

}

