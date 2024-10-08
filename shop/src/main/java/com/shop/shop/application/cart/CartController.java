package com.shop.shop.application.cart;

import com.shop.shop.application.cart.dto.CartRequestDto;
import com.shop.shop.application.cart.dto.CartResponseDto;
import com.shop.shop.domain.cart.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // 장바구니 상품 추가
    @PostMapping
    public ResponseEntity<String> addToCart(@RequestBody CartRequestDto cartRequestDto) {
        cartService.addToCart(cartRequestDto);
        return ResponseEntity.ok("Success");
    }

    // 장바구니에서 상품 삭제
    @DeleteMapping
    public ResponseEntity<String> removeFromCart(@RequestBody CartRequestDto cartRequestDto) {
        cartService.removeFromCart(cartRequestDto);
        return ResponseEntity.ok("Success");
    }

    // 장바구니 상품 수량 수정
    @PatchMapping
    public ResponseEntity<String> updateCartItemQuantity(@RequestBody CartRequestDto cartRequestDto) {
        cartService.updateCartItemQuantity(cartRequestDto);
        return ResponseEntity.ok("Success");
    }

    // 장바구니 조회
    @GetMapping("/{id}")
    public ResponseEntity<List<CartResponseDto>> getCart(@PathVariable("id") String customerId) {
        List<CartResponseDto> cartResponse = cartService.getCart(customerId);
        return ResponseEntity.ok(cartResponse);
    }

    // 장바구니 초기화
    @DeleteMapping("/clear")
    public ResponseEntity<String> clearCart(@RequestBody CartRequestDto cartRequestDto) {
        cartService.clearCart(cartRequestDto.getCustomerId());
        return ResponseEntity.ok("Success");
    }
}