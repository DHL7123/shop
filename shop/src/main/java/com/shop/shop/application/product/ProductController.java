package com.shop.shop.application.product;

import com.shop.shop.application.product.dto.ProductResponseDto;
import com.shop.shop.application.product.dto.SearchProductFilterDto;
import com.shop.shop.application.product.dto.SearchResponseDto;
import com.shop.shop.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;

    // 상품 상세페이지 조회
    @GetMapping("/{id}")
    public  ResponseEntity<ProductResponseDto> getProductDetail(@PathVariable Long id) {
        ProductResponseDto responseDto = productService.getProductDetail(id);
        return ResponseEntity.ok(responseDto);
    }

    // 상품 검색
    @PostMapping("/search")
    public ResponseEntity<SearchResponseDto> searchProducts(@RequestBody SearchProductFilterDto filterDto) {
        SearchResponseDto responseDto = productService.searchProducts(filterDto);
        return ResponseEntity.ok(responseDto);
    }

}
