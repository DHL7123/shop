package com.shop.shop.domain.product;

import com.shop.shop.application.product.dto.SearchProductFilterDto;
import com.shop.shop.application.product.dto.SearchResponseDto;
import com.shop.shop.infrastructure.persistence.product.Product;

import java.util.List;

public interface ProductService {
    SearchResponseDto searchProducts(SearchProductFilterDto filterDto);
}
