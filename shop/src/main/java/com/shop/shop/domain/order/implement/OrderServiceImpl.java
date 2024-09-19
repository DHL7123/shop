package com.shop.shop.domain.order.implement;

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


import java.time.LocalDate;
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

    @Transactional
    @Override
    public OrderResponseDto createOrder(OrderRequestDto requestDto) {
        // JWT 토큰 유효성 검사
        if (!jwtTokenProvider.validateToken(requestDto.getToken())) {
            throw new ServiceException(ExceptionList.UNSUPPORTED_TOKEN);
        }

        // 유효성 검사
        validateOrderRequest(requestDto);

        try {
            // 상품 유효성 검사
            Product product = productRepository.findById(requestDto.getProductId())
                    .orElseThrow(() -> new ServiceException(ExceptionList.NOT_EXIST_DATA));

            // 재고 유효성 검사
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

            // 주문 생성
            Orders order = Orders.create(requestDto.getProductId(), requestDto.getQuantity(), customer);
            order.setTotalPrice(calculateTotalPrice(product, requestDto.getQuantity()));  // 총 가격 계산

            orderRepository.save(order);

            return new OrderResponseDto(order);

        } catch (ServiceException e) {
            log.info("유효성 검사에 실패하였습니다: {}", e.getMessage());
            throw e;

        } catch (Exception e) {
            log.error("DB connection error: {}", e.getMessage());
            throw new ServiceException(ExceptionList.INTERNAL_SERVER_ERROR);
        }
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
    public List<OrderResponseDto> getOrdersWithConditions(Long customerId, LocalDate startDate, LocalDate endDate, OrderStatus status) {
        List<Orders> orders = orderRepository.findOrdersWithConditions(customerId, startDate, endDate, status);
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

        order.changeStatus(OrderStatus.CANCELLED);  // 주문 상태를 CANCELLED로 변경
        orderRepository.save(order);  // 주문 상태 업데이트
    }
}