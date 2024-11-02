package com.shop.shop.infrastructure.persistence.product;

import com.querydsl.core.BooleanBuilder;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shop.shop.application.product.dto.SearchProductFilterDto;
import lombok.RequiredArgsConstructor;


import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@RequiredArgsConstructor
public class CustomProductRepositoryImpl implements CustomProductRepository {

    private final JPAQueryFactory queryFactory;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Product> searchByKeywordAndCategory(SearchProductFilterDto searchProductFilterDto) {
        QProduct product = QProduct.product;
        BooleanBuilder builder = new BooleanBuilder();

        // 선택 조건: category 필터 (null이 아닐 경우에만)
        if (searchProductFilterDto.getCategory() != null && !searchProductFilterDto.getCategory().isEmpty()) {
            builder.and(product.category.eq(searchProductFilterDto.getCategory()));
        }

        // 필수 조건: keyword 필터
        builder.and(product.name.containsIgnoreCase(searchProductFilterDto.getKeyword()));

        // 선택 조건: 가격 범위 필터 (null이 아닐 경우에만)
        if (searchProductFilterDto.getMinPrice() != null) {
            builder.and(product.price.goe(searchProductFilterDto.getMinPrice())); // minPrice보다 크거나 같음
        }
        if (searchProductFilterDto.getMaxPrice() != null) {
            builder.and(product.price.loe(searchProductFilterDto.getMaxPrice())); // maxPrice보다 작거나 같음
        }

        // 선택 조건: 재고 여부 필터 (null이 아닐 경우에만)
        if (searchProductFilterDto.getInStock() != null) {
            builder.and(product.stockQuantity.gt(0)); // 재고가 있을 경우
        }



        // 쿼리 실행 및 결과 반환
        return queryFactory.selectFrom(product)
                .where(builder)
                .fetch();  // Product 타입으로 결과 리스트 가져오기
    }
}