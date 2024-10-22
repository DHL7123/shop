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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.shop.shop.infrastructure.constant.CacheConstants.REDIS_CACHE;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final RedisTemplate<String,Object> redisTemplate;  // String 형식으로 저장/조회할 RedisTemplate
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private static final String CART_KEY_PREFIX  = "cart";

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addToCart(CartRequestDto cartRequestDto) {
        //유효성 검사
        validateRequest(cartRequestDto);

        // 장바구니 키 생성
        String cartKey = CART_KEY_PREFIX + cartRequestDto.getCustomerId();

        Map<String,Object> cartItem = (Map<String, Object>) redisTemplate.opsForHash().get(cartKey, String.valueOf(cartRequestDto.getProductId()));

        if(cartItem != null) {
            // 중복된 상품 수량 증가
            int existingQuantity = (int) cartItem.get("quantity");
            cartItem.put("quantity", existingQuantity + cartRequestDto.getQuantity());
        }else{
            // 장바구니에 없는 상품 새로 추가
            cartItem = new HashMap<>();
            cartItem.put("productId", cartRequestDto.getProductId());
            cartItem.put("quantity", cartRequestDto.getQuantity());
        }
        // 레디스에 저장
        redisTemplate.opsForHash().put(cartKey, String.valueOf(cartRequestDto.getProductId()), cartItem);

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateCartItemQuantity(CartRequestDto cartRequestDto) {
        //유효성 검사
        validateRequest(cartRequestDto);

        String cartKey = CART_KEY_PREFIX + cartRequestDto.getCustomerId();

        try {
            // Redis에서 카트 아이템을 가져옵니다.
            Map<String, Object> cartItem = (Map<String, Object>) redisTemplate.opsForHash().get(cartKey, String.valueOf(cartRequestDto.getProductId()));

            if (cartItem != null) {
                // 카트 아이템 수정
                cartItem = new HashMap<>(cartItem);  // 불변성 유지
                cartItem.put("quantity", cartRequestDto.getQuantity()); // 수량 수정

                // Redis에 수정된 항목 업데이트
                redisTemplate.opsForHash().put(cartKey, String.valueOf(cartRequestDto.getProductId()), cartItem);
            } else {
                // 해당 상품이 없을 경우 예외 처리
                throw new ServiceException(ExceptionList.NOT_EXIST_DATA);
            }
        } catch (ClassCastException e) {
            // Redis 데이터 타입 불일치 시 예외 처리
            throw new ServiceException(ExceptionList.INTERNAL_SERVER_ERROR);
        }
    }

    @Transactional(readOnly = true)
    @Override
    @Cacheable(REDIS_CACHE)
    public List<CartResponseDto> getCart(String customerId) {
        // 고객 ID 유효성 검증
        if (customerId == null || customerId.isEmpty()) {
            throw new ServiceException(ExceptionList.INVALID_REQUEST);
        }

        String cartKey = CART_KEY_PREFIX + customerId;

        try {
            List<CartResponseDto> cartResponseDtoList = new ArrayList<>();
            // Redis에서 카트 아이템 가져오기
            Map<Object, Object> cartItems = (Map<Object, Object>) redisTemplate.opsForHash().entries(cartKey);

            // 카트가 비어있는지 확인
            if (cartItems == null || cartItems.isEmpty()) {
                throw new ServiceException(ExceptionList.NOT_EXIST_DATA);
            }

            // 카트 아이템을 DTO로 변환
            for (Map.Entry<Object, Object> entry : cartItems.entrySet()) {
                Map<String, Object> itemData = (Map<String, Object>) entry.getValue();
                Long productId = Long.valueOf(itemData.get("productId").toString());
                Integer quantity = Integer.valueOf(itemData.get("quantity").toString());

                // 상품 정보 조회
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new ServiceException(ExceptionList.NOT_EXIST_DATA));

                // 카트 응답 DTO 생성
                cartResponseDtoList.add(new CartResponseDto(product, quantity));
            }

            return cartResponseDtoList;

        } catch (ClassCastException e) {
            // Redis에서 타입 변환 오류 시 예외 처리
            throw new ServiceException(ExceptionList.INTERNAL_SERVER_ERROR);
        } catch (NumberFormatException e) {
            // 숫자 변환 오류 시 예외 처리
            throw new ServiceException(ExceptionList.BAD_REQUEST);
        }
    }

    @Transactional
    @Override
    public void clearCart(String customerId) {
        String cartKey = CART_KEY_PREFIX + customerId;
        redisTemplate.delete(cartKey);

    }

    @Transactional
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
            } else {
                // 수량이 0이하일 경우 Redis에서 항목 삭제
                redisTemplate.opsForHash().delete(cartKey, String.valueOf(cartRequestDto.getProductId()));
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
            throw new ServiceException(ExceptionList.INVALID_REQUEST);
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ServiceException(ExceptionList.NOT_EXIST_DATA));

        // 상품 재고 확인
        if (product.getStockQuantity() < requestedQuantity) {
            throw new ServiceException(ExceptionList.NOT_ENOUGH_STOCK);
        }
    }

    // 고객 유효성 검증 (계정 존재 여부 확인)
    private void validateCustomer(String customerId) {
        if (customerId == null || customerId.isEmpty()) {
            throw new ServiceException(ExceptionList.INVALID_REQUEST);
        }

        boolean customerExists = customerRepository.existsByCustomerId(customerId);
        if (!customerExists) {
            throw new ServiceException(ExceptionList.NOT_EXIST_CUSTOMER_ACCOUNT);
        }
    }
}
