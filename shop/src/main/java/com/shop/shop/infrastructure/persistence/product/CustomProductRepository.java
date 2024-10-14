package com.shop.shop.infrastructure.persistence.product;

import com.shop.shop.application.product.dto.SearchProductFilterDto;

import java.util.List;

public interface CustomProductRepository {
    List<Product> searchByKeywordAndCategory(SearchProductFilterDto searchProductFilterDto);
}
