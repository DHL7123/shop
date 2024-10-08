package com.shop.shop.application.cart.dto;

import com.shop.shop.infrastructure.persistence.product.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartResponseDto {
    private Product product;
    private int quantity;
}
