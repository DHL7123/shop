package com.shop.shop.application.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartRequestDto {
    private String customerId;
    private Long productId;
    private int quantity;
}
