package com.shop.shop.domain.product;

import com.shop.shop.application.product.dto.SearchProductFilterDto;
import com.shop.shop.application.product.dto.SearchResponseDto;
import com.shop.shop.domain.product.implement.ProductServiceImpl;
import com.shop.shop.infrastructure.exception.ExceptionList;
import com.shop.shop.infrastructure.exception.ServiceException;
import com.shop.shop.infrastructure.persistence.product.Product;
import com.shop.shop.infrastructure.persistence.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private SearchProductFilterDto filterDto;
    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        filterDto = new SearchProductFilterDto("laptop", "electronics", 500L, 1500L, true);
        product1 = new Product(1L, "Gaming Laptop", "High performance laptop", "electronics", 10L, 1200L, "Fast shipping");
        product2 = new Product(2L, "Business Laptop", "Efficient and lightweight", "electronics", 5L, 1000L, "Standard shipping");
    }

    @DisplayName("SearchProducts - 필터 조건을 만족하는 제품 검색 성공 (Success)")
    @Test
    void testSearchProducts_Success() {
        when(productRepository.searchByKeywordAndCategory(filterDto)).thenReturn(Arrays.asList(product1, product2));

        SearchResponseDto response = productService.searchProducts(filterDto);

        assertAll("검색 결과 확인",
                () -> assertNotNull(response),
                () -> assertEquals(2, response.getProducts().size(), "제품 수 검증"),

                // 가격 검증을 계층형으로 구조화
                () -> assertAll("가격 정보 검증",
                        () -> assertEquals(1000L, response.getMinPrice(), "최소 가격 검증"),
                        () -> assertEquals(1200L, response.getMaxPrice(), "최대 가격 검증")
                ),

                // 재고 상태 검증
                () -> assertTrue(response.getStockAvailability().contains(true), "재고 상태 확인")
        );

        verify(productRepository, times(1)).searchByKeywordAndCategory(filterDto);
    }

    @DisplayName("SearchProducts - 필터 조건에 맞는 제품이 없을 때 빈 결과 반환 (No Products Found)")
    @Test
    void testSearchProducts_NoProductsFound() {
        when(productRepository.searchByKeywordAndCategory(filterDto)).thenReturn(Collections.emptyList());

        SearchResponseDto response = productService.searchProducts(filterDto);

        assertNotNull(response);
        assertTrue(response.getProducts().isEmpty());
        assertEquals(0L, response.getMinPrice());
        assertEquals(0L, response.getMaxPrice());
        assertTrue(response.getStockAvailability().isEmpty());
        verify(productRepository, times(1)).searchByKeywordAndCategory(filterDto);
    }

    @DisplayName("SearchProducts - 필수 조건인 키워드가 누락된 경우 예외 발생 (Missing Keyword)")
    @Test
    void testSearchProducts_MissingKeyword() {
        filterDto.setKeyword(null); // 필수 조건인 키워드를 null로 설정

        assertThrows(ServiceException.class, () -> productService.searchProducts(filterDto));
        verify(productRepository, never()).searchByKeywordAndCategory(filterDto);
    }
}