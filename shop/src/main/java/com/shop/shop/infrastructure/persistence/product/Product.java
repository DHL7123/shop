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
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private Long stockQuantity;

    @Column(nullable = false)
    private Long price; // 가격은 0보다 커야 함

    @Column(nullable = false)
    private String shipping;

    public Product(long id, String name, long stockQuantity) {
        this.id = id;
        this.name = name;
        this.stockQuantity = stockQuantity;
    }

    // 재고 감소
    public void decreaseStockQuantity(Long stockQuantity) {
        validateStockQuantity(stockQuantity);
        if (this.stockQuantity < stockQuantity) {
            throw new IllegalArgumentException("재고 수량이 부족합니다.");
        }
        this.stockQuantity -= stockQuantity;
    }

    // 재고 증가
    public void increaseStock(Long stockQuantity) {
        validateStockQuantity(stockQuantity);
        this.stockQuantity += stockQuantity;
    }

    // 유효성 검사
    private void validateStockQuantity(Long stockQuantity) {
        if (stockQuantity == null || stockQuantity <= 0) {
            throw new IllegalArgumentException("재고 수량은 0보다 커야 합니다.");
        }
    }

    // 재고가 있는지 여부를 반환하는 메서드
    public boolean isInStock() {
        return this.stockQuantity > 0;  // 재고가 0보다 클 경우 true 반환
    }

}
