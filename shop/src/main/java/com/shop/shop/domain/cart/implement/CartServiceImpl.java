package com.shop.shop.domain.cart.implement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.shop.application.cart.dto.CartRequestDto;
import com.shop.shop.application.cart.dto.CartResponseDto;
import com.shop.shop.application.order.dto.request.OrderRequestDto;
import com.shop.shop.domain.cart.CartService;
import com.shop.shop.infrastructure.exception.ExceptionList;
import com.shop.shop.infrastructure.exception.ServiceException;
import com.shop.shop.infrastructure.persistence.member.Customer;
import com.shop.shop.infrastructure.persistence.member.CustomerRepository;
import com.shop.shop.infrastructure.persistence.product.Product;
import com.shop.shop.infrastructure.persistence.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final RedisTemplate<String,Object> redisTemplate;  // String 형식으로 저장/조회할 RedisTemplate
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private static final String CART_KEY_PREFIX  = "cart";

    @Override
    public void addToCart(CartRequestDto cartRequestDto) {
        //유효성 검사
        validateRequest(cartRequestDto);

        // 장바구니 키 생성
        String cartKey = CART_KEY_PREFIX + cartRequestDto.getCustomerId();

        // 로그 추가: Redis 키와 값이 어떻게 저장되는지 확인
        log.info("Cart Key: " + cartKey);
        log.info("Product ID: " + cartRequestDto.getProductId() + ", Quantity: " + cartRequestDto.getQuantity());

        Map<String,Object> cartItem = (Map<String, Object>) redisTemplate.opsForHash().get(cartKey, String.valueOf(cartRequestDto.getProductId()));

        if(cartItem != null) {
            // 중복된 상품 수량 증가
            int existingQuantity = (int) cartItem.get("quantity");
            cartItem.put("quantity", existingQuantity + cartRequestDto.getQuantity());
            log.info("updated quantity for product" + cartRequestDto.getProductId());
        }else{
            // 장바구니에 없는 상품 새로 추가
            cartItem = new HashMap<>();
            cartItem.put("productId", cartRequestDto.getProductId());
            cartItem.put("quantity", cartRequestDto.getQuantity());
            log.info("added new product to cart" + cartItem);
        }
        // 레디스에 저장
        redisTemplate.opsForHash().put(cartKey, String.valueOf(cartRequestDto.getProductId()), cartItem);

    }


    @Override
    public void updateCartItemQuantity(CartRequestDto cartRequestDto) {
        //유효성 검사
        validateRequest(cartRequestDto);

        String cartKey = CART_KEY_PREFIX + cartRequestDto.getCustomerId();

        try{
            Map<String, Object> cartItem = (Map<String, Object>) redisTemplate.opsForHash().get(cartKey, String.valueOf(cartRequestDto.getProductId()));
            if (cartItem != null) {

                cartItem = new HashMap<>(cartItem);
                cartItem.put("quantity", cartRequestDto.getQuantity()); // 수량 수정
                redisTemplate.opsForHash().put(cartKey, String.valueOf(cartRequestDto.getProductId()), cartItem);
            }else {
                throw new IllegalArgumentException("No data ProductId: " + cartRequestDto.getProductId());
            }
        }catch (ClassCastException e){
            throw new IllegalArgumentException("타입 불일치 오류",e);
        }
    }


    @Override
    public List<CartResponseDto> getCart(String customerId) {
        String cartKey = CART_KEY_PREFIX + customerId;

        try {
            List<CartResponseDto> cartResponseDtoList = new ArrayList<>();
            Map<Object, Object> cartItems = (Map<Object, Object>) redisTemplate.opsForHash().entries(cartKey);
            for (Map.Entry<Object, Object> entry : cartItems.entrySet()) {
                Map<String, Object> itemData = (Map<String, Object>) entry.getValue();
                Long productId = Long.valueOf(itemData.get("productId").toString());
                Integer quantity = Integer.valueOf(itemData.get("quantity").toString());

                Product product = productRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("No data"));
                cartResponseDtoList.add(new CartResponseDto(product, quantity));
            }
            return cartResponseDtoList;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("타입 불일치");
        }
    }

    @Override
    public void clearCart(String customerId) {
        String cartKey = CART_KEY_PREFIX + customerId;
        redisTemplate.delete(cartKey);

    }

    @Override
    public void removeFromCart(CartRequestDto cartRequestDto) {

        String cartKey = CART_KEY_PREFIX + cartRequestDto.getCustomerId();

        // 장바구니 조회
        Map<String, Object> cartItem = (Map<String, Object>) redisTemplate.opsForHash().get(cartKey, String.valueOf(cartRequestDto.getProductId()));

        if (cartItem != null) {
            int currentQuantity = (int) cartItem.get("quantity");
            int newQuantity = currentQuantity - cartRequestDto.getQuantity();

            if (newQuantity > 0) {
                // 수량이 남아있을 경우
                cartItem.put("quantity", newQuantity);
                redisTemplate.opsForHash().put(cartKey, String.valueOf(cartRequestDto.getProductId()), cartItem);
                log.info("Updated quantity for product " + cartRequestDto.getProductId() + ", Quantity: " + newQuantity);
            } else {
                // 수량이 0이하일 경우 Redis에서 항목 삭제
                redisTemplate.opsForHash().delete(cartKey, String.valueOf(cartRequestDto.getProductId()));
                log.info("Product " + cartRequestDto.getProductId() + " removed from cart.");
            }

        } else {
            throw new ServiceException(ExceptionList.NOT_EXIST_DATA);
        }
    }

    // 유효성 검증 메서드
    private void validateRequest(CartRequestDto cartRequestDto) {
        validateCustomer(cartRequestDto.getCustomerId());
        validateProduct(cartRequestDto.getProductId(), cartRequestDto.getQuantity());
    }

    // 상품 유효성 검증 (상품 존재 여부 및 재고 확인)
    private void validateProduct(Long productId, int requestedQuantity) {
        if (productId == null || productId <= 0) {
            throw new ServiceException(ExceptionList.BAD_REQUEST);
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ServiceException(ExceptionList.NOT_EXIST_DATA));

        // 상품의 재고 확인
        if (product.getStockQuantity() < requestedQuantity) {
            throw new ServiceException(ExceptionList.BAD_REQUEST);
        }
    }

    // 고객 유효성 검증 (계정 존재 여부 확인)
    private void validateCustomer(String customerId) {
        if (customerId == null || customerId.isEmpty()) {
            throw new ServiceException(ExceptionList.BAD_REQUEST);
        }

        boolean customerExists = customerRepository.existsByCustomerId(customerId);
        if (!customerExists) {
            throw new ServiceException(ExceptionList.NOT_EXIST_DATA);
        }
    }

}
