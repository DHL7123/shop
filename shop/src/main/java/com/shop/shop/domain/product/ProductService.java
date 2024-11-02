package com.shop.shop.domain.product;

import com.shop.shop.application.product.dto.ProductResponseDto;
import com.shop.shop.application.product.dto.SearchProductFilterDto;
import com.shop.shop.application.product.dto.SearchResponseDto;

public interface ProductService {
    SearchResponseDto searchProducts(SearchProductFilterDto filterDto);
    ProductResponseDto getProductDetail(Long id);
}
