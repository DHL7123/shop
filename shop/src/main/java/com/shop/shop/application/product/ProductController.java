package com.shop.shop.application.product;

import com.shop.shop.application.product.dto.SearchProductFilterDto;
import com.shop.shop.application.product.dto.SearchResponseDto;
import com.shop.shop.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;

    @PostMapping("/search")
    public SearchResponseDto searchProducts(@RequestBody SearchProductFilterDto filterDto) {
        return productService.searchProducts(filterDto);

    }
}
