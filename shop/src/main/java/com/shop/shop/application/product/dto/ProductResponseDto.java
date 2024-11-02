package com.shop.shop.application.product.dto;

import com.shop.shop.infrastructure.persistence.product.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponseDto {
    private Long id;
    private String name;
    private String description;
    private String category;
    private Long price;

    public ProductResponseDto(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.category = product.getCategory();
        this.price = product.getPrice();
    }
}
