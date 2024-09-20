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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<OrderResponseDto> createMultipleOrders(List<OrderRequestDto> orderRequestDtos) {
        List<OrderResponseDto> responseDtos = new ArrayList<>();

        for (OrderRequestDto requestDto : orderRequestDtos) {
            OrderResponseDto responseDto = createOrder(requestDto);
            responseDtos.add(responseDto);
        }

        return responseDtos;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public OrderResponseDto createOrder(OrderRequestDto requestDto) {
        // JWT 토큰 유효성 검사
        if (!jwtTokenProvider.validateToken(requestDto.getToken())) {
            throw new ServiceException(ExceptionList.UNSUPPORTED_TOKEN);
        }

        // 유효성 검사
        validateOrderRequest(requestDto);

        try {
            // 상품 유효성 및 재고 검사
            Product product = productRepository.findById(requestDto.getProductId())
                    .orElseThrow(() -> new ServiceException(ExceptionList.NOT_EXIST_DATA));

            if (product.getStockQuantity() < requestDto.getQuantity()) {
                throw new ServiceException(ExceptionList.BAD_REQUEST);
            }

            // 재고 감소
            product.decreaseStockQuantity(requestDto.getQuantity());
            productRepository.save(product);

            // 고객 유효성 검사
            String customerId = jwtTokenProvider.getCustomerIdFromToken(requestDto.getToken());
            Customer customer = customerRepository.findByCustomerId(customerId)
                    .orElseThrow(() -> new ServiceException(ExceptionList.NOT_EXIST_CUSTOMER_ACCOUNT));

            // 주문 생성 비즈니스 로직을 서비스에서 처리
            Orders order = createOrderEntity(requestDto, customer, product);
            orderRepository.save(order);

            return new OrderResponseDto(order);

        } catch (ServiceException e) {
            log.info("주문 생성 중 유효성 검사 실패: {}", e.getMessage());
            throw e;

        } catch (Exception e) {
            log.error("DB 처리 오류: {}", e.getMessage());
            throw new ServiceException(ExceptionList.INTERNAL_SERVER_ERROR);
        }
    }

    // 주문 생성 비즈니스 로직
    private Orders createOrderEntity(OrderRequestDto requestDto, Customer customer, Product product) {
        validateOrder(requestDto.getProductId(), requestDto.getQuantity());
        Orders order = new Orders();
        order.setProductId(requestDto.getProductId());
        order.setQuantity(requestDto.getQuantity());
        order.setCustomer(customer);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalPrice(calculateTotalPrice(product, requestDto.getQuantity()));
        return order;
    }

    // 유효성 검사 메서드
    private void validateOrderRequest(OrderRequestDto requestDto) {
        if (requestDto.getProductId() == null || requestDto.getProductId() <= 0) {
            throw new ServiceException(ExceptionList.BAD_REQUEST);
        }

        if (requestDto.getQuantity() == null || requestDto.getQuantity() <= 0) {
            throw new ServiceException(ExceptionList.BAD_REQUEST);
        }

        if (requestDto.getCustomerId() == null) {
            throw new ServiceException(ExceptionList.NOT_EXIST_CUSTOMER_ACCOUNT);
        }
    }

    // 총 가격 계산 메서드
    private Long calculateTotalPrice(Product product, Long quantity) {
        if (quantity <= 0) {
            throw new ServiceException(ExceptionList.BAD_REQUEST);
        }
        return product.getPrice() * quantity;
    }


    @Transactional(readOnly = true)
    @Override
    @Cacheable(value = "orders", key = "#orderId")
    public OrderResponseDto getOrder(Long orderId) {

        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ServiceException(ExceptionList.NOT_EXIST_DATA));  // 주문이 없을 경우
        return new OrderResponseDto(order);
    }

    @Transactional(readOnly = true)
    @Override
    public List<OrderResponseDto> getAllOrders() {
        List<Orders> orders = orderRepository.findAllWithCustomer();  // 주문 목록 조회
        if (orders.isEmpty()) {
            throw new ServiceException(ExceptionList.NOT_EXIST_DATA);  // 데이터가 없을 경우
        }
        return orders.stream().map(OrderResponseDto::new).collect(Collectors.toList());
    }

    @Transactional
    @Override
    @CacheEvict(value = "orders", key = "#orderId")
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
    @CacheEvict(value = "orders", key = "#orderId")
    public void cancelOrder(Long orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ServiceException(ExceptionList.NOT_EXIST_DATA));  // 주문이 없을 경우

        Product product = productRepository.findById(order.getProductId())
                .orElseThrow(() -> new ServiceException(ExceptionList.NOT_EXIST_DATA));  // 상품이 없을 경우

        product.increaseStock(order.getQuantity());  // 주문 취소 시 재고 복구
        productRepository.save(product);  // 재고 저장

        order.setStatus(OrderStatus.CANCELLED);  // 주문 상태를 CANCELLED로 변경
        orderRepository.save(order);  // 주문 상태 업데이트
    }

    // 주문 유효성 검사 메서드
    private void validateOrder(Long productId, Long quantity) {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 제품 ID입니다.");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("수량은 0보다 커야 합니다.");
        }
    }
}
