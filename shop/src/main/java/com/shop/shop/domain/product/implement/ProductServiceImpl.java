package com.shop.shop.domain.product.implement;

import com.shop.shop.application.product.dto.ProductResponseDto;
import com.shop.shop.application.product.dto.SearchProductFilterDto;
import com.shop.shop.application.product.dto.SearchResponseDto;
import com.shop.shop.domain.product.ProductService;
import com.shop.shop.infrastructure.exception.ExceptionList;
import com.shop.shop.infrastructure.exception.ServiceException;
import com.shop.shop.infrastructure.persistence.product.Product;
import com.shop.shop.infrastructure.persistence.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
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
    private final RedisTemplate<String, Long> redisTemplate;
    private final RedisTemplate<String, Long> redisTemplateForInteger;

    @Transactional(readOnly = true)
    @Cacheable(REDIS_CACHE)
    @Override
    public SearchResponseDto searchProducts(SearchProductFilterDto filterDto) {
        if (filterDto.getKeyword() == null || filterDto.getKeyword().isEmpty()) {
            throw new ServiceException(ExceptionList.BAD_REQUEST);
        }

        List<Product> products = productRepository.searchByKeywordAndCategory(filterDto);

        if (products.isEmpty()) {
            return new SearchResponseDto(Collections.emptyList(), 0L, 0L, Collections.emptyList());
        }

        Long minPrice = products.stream().map(Product::getPrice).min(Long::compare).orElse(0L);
        Long maxPrice = products.stream().map(Product::getPrice).max(Long::compare).orElse(0L);
        List<Boolean> stockAvailability = products.stream().map(Product::isInStock).collect(Collectors.toList());

        return new SearchResponseDto(products, minPrice, maxPrice, stockAvailability);
    }

    @Transactional(readOnly = true)
    @Override
    public ProductResponseDto getProductDetail(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ServiceException(ExceptionList.NOT_EXIST_DATA));
        log.info("Calling increaseViewCount for product ID: {}", id);
        increaseViewCount(id);
        return new ProductResponseDto(product);
    }

    public void increaseViewCount(Long id) {
        String key = "product:view" + id;

            Long viewCount = redisTemplate.opsForValue().get(key);

            if (viewCount == null) {
                redisTemplate.opsForValue().set(key, 1L); // 초기 값 설정
            } else {
                redisTemplateForInteger.opsForValue().increment(key); // 조회수 증가
            }

        }
    }
