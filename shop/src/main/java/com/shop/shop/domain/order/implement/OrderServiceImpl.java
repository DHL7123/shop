package com.shop.shop.domain.order.implement;

import com.shop.shop.application.order.dto.request.OrderConditionDto;
import com.shop.shop.application.order.dto.request.OrderRequestDto;
import com.shop.shop.application.order.dto.response.OrderResponseDto;
import com.shop.shop.domain.order.OrderService;
import com.shop.shop.infrastructure.authentication.JwtTokenProvider;
import com.shop.shop.infrastructure.constant.OrderStatus;
import com.shop.shop.infrastructure.exception.ExceptionList;
import com.shop.shop.infrastructure.exception.ServiceException;
import com.shop.shop.infrastructure.persistence.member.Customer;
import com.shop.shop.infrastructure.persistence.member.CustomerRepository;
import com.shop.shop.infrastructure.persistence.order.OrderRepository;
import com.shop.shop.infrastructure.persistence.order.Orders;
import com.shop.shop.infrastructure.persistence.product.Product;
import com.shop.shop.infrastructure.persistence.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.shop.shop.infrastructure.constant.CacheConstants.REDIS_CACHE;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {


    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String ORDER_PREFIX = "order:";


    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<OrderResponseDto> createOrders(List<OrderRequestDto> orderRequestDtos) {
        List<OrderResponseDto> responseDtos = new ArrayList<>();
        List<Long> processedProductIds = new ArrayList<>();

        for (OrderRequestDto requestDto : orderRequestDtos) {
            // 토큰 검증
            if (!jwtTokenProvider.validateToken(requestDto.getToken())) {
                throw new ServiceException(ExceptionList.UNSUPPORTED_TOKEN);
            }
            // 상품 조회 및 예외 처리
            Product product = productRepository.findById(requestDto.getProductId())
                    .orElseThrow(() ->
                            new ServiceException(ExceptionList.NOT_EXIST_DATA)
                    );

            // 재고 확인 및 차감
            if (product.getStockQuantity() < requestDto.getQuantity()) {
                throw new ServiceException(ExceptionList.NOT_ENOUGH_STOCK);
            }
            product.decreaseStockQuantity(requestDto.getQuantity());
            productRepository.save(product);

            // 고객 정보 조회 및 설정
            Customer customer = customerRepository.findByCustomerId(requestDto.getCustomerId())
                    .orElseThrow(() ->
                            new ServiceException(ExceptionList.NOT_EXIST_DATA)
                    );

            // 주문 생성
            Orders newOrder = new Orders(
                    product,
                    requestDto.getQuantity(),
                    customer,
                    OrderStatus.PENDING,
                    LocalDateTime.now()
            );
            orderRepository.save(newOrder);  // 주문 저장

            // 응답 DTO 생성 및 추가
            OrderResponseDto responseDto = new OrderResponseDto(newOrder);
            responseDtos.add(responseDto);  // 응답 리스트에 추가

            processedProductIds.add(requestDto.getProductId());
        }

        return responseDtos;
    }


    @Transactional(readOnly = true)
    @Override
    @Cacheable(REDIS_CACHE)
    public OrderResponseDto getOrder(Long orderId) {
        // Redis에 없으면 DB에서 조회
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ServiceException(ExceptionList.NOT_EXIST_DATA));
        OrderResponseDto orderResponseDto = new OrderResponseDto(order);
        return orderResponseDto;
    }


    @Transactional(readOnly = true)
    @Override
    @Cacheable(REDIS_CACHE)
    public List<OrderResponseDto> getAllOrders() {
        List<Orders> orders = orderRepository.findAllWithCustomer();  // 주문 목록 조회
        if (orders.isEmpty()) {
            throw new ServiceException(ExceptionList.NOT_EXIST_DATA);  // 데이터가 없을 경우
        }
        return orders.stream().map(OrderResponseDto::new).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public OrderResponseDto updateOrder(Long orderId, OrderRequestDto requestDto) {
        // JWT 토큰 유효성 검사
        if (!jwtTokenProvider.validateToken(requestDto.getToken())) {
            throw new ServiceException(ExceptionList.UNSUPPORTED_TOKEN);
        }

        Orders existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new ServiceException(ExceptionList.NOT_EXIST_DATA));  // 주문이 없을 경우

        Product product = productRepository.findById(existingOrder.getProductId())
                .orElseThrow(() -> new ServiceException(ExceptionList.NOT_EXIST_DATA));  // 상품이 없을 경우

        // 고객 유효성 검사
        String customerId = jwtTokenProvider.getCustomerIdFromToken(requestDto.getToken());
        Customer customer = customerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ServiceException(ExceptionList.NOT_EXIST_CUSTOMER_ACCOUNT));


        Long quantityDiff = existingOrder.getQuantity() - requestDto.getQuantity();

        // 재고 처리 로직
        if (quantityDiff > 0) {
            product.decreaseStockQuantity(quantityDiff);
        } else if (quantityDiff < 0) {
            product.increaseStock(-quantityDiff);
        }
        productRepository.save(product);  // 재고 변경 사항 저장

        existingOrder.setQuantity(requestDto.getQuantity());
        orderRepository.save(existingOrder);  // 주문 수정


        return new OrderResponseDto(existingOrder);
    }


    @Transactional(readOnly = true)
    @Override
    @Cacheable(REDIS_CACHE)
    public List<OrderResponseDto> getOrdersWithConditions(OrderConditionDto conditionDto) {
        List<Orders> orders = orderRepository.findOrdersWithConditions(
                conditionDto.getCustomerId(),
                conditionDto.getStartDate(),
                conditionDto.getEndDate(),
                conditionDto.getStatus()
        );

        if (orders.isEmpty()) {
            throw new ServiceException(ExceptionList.NOT_EXIST_DATA);
        }

        return orders.stream().map(OrderResponseDto::new).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void cancelOrder(Long orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ServiceException(ExceptionList.NOT_EXIST_DATA));  // 주문이 없을 경우

        Product product = productRepository.findById(order.getProductId())
                .orElseThrow(() -> new ServiceException(ExceptionList.NOT_EXIST_DATA));  // 상품이 없을 경우

        product.increaseStock(order.getQuantity());  // 주문 취소 시 재고 복구
        productRepository.save(product);  // 재고 저장

        order.setStatus(OrderStatus.CANCELLED);  // 주문 상태를 CANCELLED로 변경
        orderRepository.save(order);  // 주문 상태 업데이트

        // 캐시 삭제
        String cacheKey = ORDER_PREFIX + orderId;
        redisTemplate.delete(cacheKey);
    }

}
