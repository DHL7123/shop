package com.shop.shop.domain.cart;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.shop.shop.application.cart.dto.CartRequestDto;
import com.shop.shop.application.cart.dto.CartResponseDto;
import com.shop.shop.domain.cart.implement.CartServiceImpl;
import com.shop.shop.infrastructure.exception.ServiceException;
import com.shop.shop.infrastructure.persistence.member.CustomerRepository;
import com.shop.shop.infrastructure.persistence.product.Product;
import com.shop.shop.infrastructure.persistence.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class CartServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private HashOperations<String, String, Object> hashOperations;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private CartRequestDto cartRequestDto;
    private Product product;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        cartRequestDto = new CartRequestDto("customer123", 1L, 2); // 고객 ID, 상품 ID, 수량 설정
        product = new Product(1L, "Test Product", 100); // 상품 객체

        // RedisTemplate의 opsForHash가 HashOperations Mock을 반환하도록 설정
        doReturn(hashOperations).when(redisTemplate).opsForHash();

        // CustomerRepository의 existsByCustomerId 메서드 Mock 설정
        when(customerRepository.existsByCustomerId(cartRequestDto.getCustomerId())).thenReturn(true); // 고객이 존재하는 경우
    }

    @DisplayName("AddToCart - 정상적으로 장바구니 새상품 추가 (Success)")
    @Test
    void testAddToCartNewItem() {
        // Given
        when(productRepository.findById(cartRequestDto.getProductId())).thenReturn(Optional.of(product));
        when(hashOperations.get(anyString(), anyString())).thenReturn(null); // 새 상품

        // When
        cartService.addToCart(cartRequestDto);

        // Then
        verify(hashOperations, times(1)).put(anyString(), anyString(), any());
    }

    @DisplayName("UpdateCartItemQuantity - 정상적으로 장바구니 상품 수량 수정 (Success)")
    @Test
    void testUpdateCartItemQuantity(){
        // Given
        Map<String, Object> cartItem = new HashMap<>();
        cartItem.put("productId", cartRequestDto.getProductId());
        cartItem.put("quantity", 5);

        when(productRepository.findById(cartRequestDto.getProductId())).thenReturn(Optional.of(product));
        when(hashOperations.get(anyString(), anyString())).thenReturn(cartItem);

        // When
        cartService.updateCartItemQuantity(cartRequestDto);

        // Then
        verify(hashOperations, times(1)).put(anyString(), anyString(), any());
    }

    @DisplayName("GetCart - 장바구니 조회 성공 (Success)")
    @Test
    void testGetCartSuccess() {
        // Given
        Map<String, Object> cartItems = new HashMap<>();
        Map<String, Object> cartItem = new HashMap<>();
        cartItem.put("productId", 1L);
        cartItem.put("quantity", 2);
        cartItems.put("1", cartItem);

        when(hashOperations.entries(anyString())).thenReturn(cartItems);
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));

        // When
        List<CartResponseDto> cartResponse = cartService.getCart("customer123");

        // Then
        assertEquals(1, cartResponse.size());
        assertEquals(product.getName(), cartResponse.get(0).getProduct().getName());
    }

    @DisplayName("ClearCart - 장바구니 비우기 (Success)")
    @Test
    void testClearCart() {
        // Given
        String customerId = "customer123";

        // When
        cartService.clearCart(customerId);

        // Then
        verify(redisTemplate, times(1)).delete("cart" + customerId);
    }

    @DisplayName("RemoveFromCart - 장바구니에서 상품 수량 감소 (Decrease Quantity)")
    @Test
    void testRemoveFromCart_DecreaseQuantity() {
        // Given
        Map<String, Object> cartItem = new HashMap<>();
        cartItem.put("productId", cartRequestDto.getProductId());
        cartItem.put("quantity", 5); // 기존 수량이 5

        when(hashOperations.get(anyString(), anyString())).thenReturn(cartItem);

        // When
        cartService.removeFromCart(cartRequestDto);

        // Then
        verify(hashOperations, times(1)).put(anyString(), anyString(), any());
    }

    @DisplayName("RemoveFromCart - 장바구니에서 상품 삭제 (Remove Item)")
    @Test
    void testRemoveFromCart_RemoveItem() {
        // Given
        Map<String, Object> cartItem = new HashMap<>();
        cartItem.put("productId", cartRequestDto.getProductId());
        cartItem.put("quantity", 2); // 수량이 2이므로 감소 후 삭제됨

        when(hashOperations.get(anyString(), anyString())).thenReturn(cartItem);

        // When
        cartService.removeFromCart(cartRequestDto);

        // Then
        verify(hashOperations, times(1)).delete(anyString(), anyString());
    }
}