package com.shop.shop.domain.cart;

import com.shop.shop.application.cart.dto.CartRequestDto;
import com.shop.shop.application.cart.dto.CartResponseDto;

import java.util.List;
import java.util.Map;

public interface CartService {

    void addToCart(CartRequestDto cartRequestDto);
    void removeFromCart(CartRequestDto cartRequestDto);
    void updateCartItemQuantity(CartRequestDto cartRequestDto);
    List<CartResponseDto> getCart(String customerId);
    void clearCart(String customerId);

}
