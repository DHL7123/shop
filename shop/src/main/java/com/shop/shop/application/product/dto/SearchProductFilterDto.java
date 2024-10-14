package com.shop.shop.application.product.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchProductFilterDto {

    @NotEmpty(message = "검색어는 필수입니다.")
    private String keyword;

    private String category;
    private Long minPrice;
    private Long maxPrice;
    private Boolean inStock;
}
