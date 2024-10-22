package com.shop.shop.domain.product.implement;

import com.shop.shop.application.product.dto.SearchProductFilterDto;
import com.shop.shop.application.product.dto.SearchResponseDto;
import com.shop.shop.domain.product.ProductService;
import com.shop.shop.infrastructure.persistence.product.Product;
import com.shop.shop.infrastructure.persistence.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.shop.shop.infrastructure.constant.CacheConstants.REDIS_CACHE;


@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    @Cacheable(REDIS_CACHE)
    @Override
    public SearchResponseDto searchProducts(SearchProductFilterDto filterDto) {
        // 키워드와 카테고리로 제품 검색
        List<Product> products = productRepository.searchByKeywordAndCategory(filterDto);

        if (products.isEmpty()) {
            return new SearchResponseDto(Collections.emptyList(), 0L, 0L, Collections.emptyList());
        }

        Long minPrice = products.stream().map(Product::getPrice).min(Long::compare).orElse(0L);
        Long maxPrice = products.stream().map(Product::getPrice).max(Long::compare).orElse(0L);
        List<Boolean> stockAvailability = products.stream().map(Product::isInStock).collect(Collectors.toList());

        return new SearchResponseDto(products, minPrice, maxPrice, stockAvailability);
    }

}
