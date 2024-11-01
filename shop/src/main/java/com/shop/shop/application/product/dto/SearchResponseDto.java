package com.shop.shop.application.product.dto;

import com.shop.shop.infrastructure.persistence.product.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchResponseDto {
    private List<Product> products;
    private Long minPrice;
    private Long maxPrice;
    private List<Boolean> stockAvailability;
}
