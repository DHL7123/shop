package com.shop.shop.domain.order;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.shop.shop.application.member.dto.request.SignupRequestDto;
import com.shop.shop.application.order.dto.request.OrderConditionDto;
import com.shop.shop.application.order.dto.request.OrderRequestDto;
import com.shop.shop.application.order.dto.response.OrderResponseDto;
import com.shop.shop.domain.order.implement.OrderServiceImpl;
import com.shop.shop.infrastructure.authentication.JwtTokenProvider;
import com.shop.shop.infrastructure.constant.OrderStatus;
import com.shop.shop.infrastructure.exception.ServiceException;
import com.shop.shop.infrastructure.persistence.member.Customer;
import com.shop.shop.infrastructure.persistence.member.CustomerRepository;
import com.shop.shop.infrastructure.persistence.order.OrderRepository;
import com.shop.shop.infrastructure.persistence.order.Orders;
import com.shop.shop.infrastructure.persistence.product.Product;
import com.shop.shop.infrastructure.persistence.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

class OrderServiceTest {
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Product product;
    private Customer customer;
    private Orders order;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // 테스트용 Product와 Customer 객체 설정
        product = new Product(1L, "Test Product", 100);

        // SignupRequestDto를 이용해 Customer 초기화
        SignupRequestDto signupRequestDto = new SignupRequestDto("customer123", "1234", "John Doe", "john@example.com", "1234567890");
        customer = Customer.initializeCustomer(signupRequestDto, "encryptedPassword");

        order = new Orders(product, 2L, customer, OrderStatus.PENDING, LocalDateTime.now());

    }


    @DisplayName("CreateOrders - 정상적으로 주문 생성 (Success)")
    @Test
    void testCreateOrdersSuccess() {
        // Given
        List<OrderRequestDto> orderRequestDtos = List.of(
                new OrderRequestDto(1L, 2L, customer.getCustomerId(), "validToken")
        );
        when(jwtTokenProvider.validateToken(anyString())).thenReturn(true); // 토큰 검증 통과
        when(productRepository.findByIdWithPessimisticLock(anyLong())).thenReturn(Optional.of(product));
        when(customerRepository.findByCustomerId(anyString())).thenReturn(Optional.of(customer));
        when(orderRepository.save(any(Orders.class))).thenReturn(order);

        // When
        List<OrderResponseDto> responses = orderService.createOrders(orderRequestDtos);

        // Then
        assertEquals(1, responses.size());
        assertEquals(1, responses.get(0).getProductId());
    }

    @DisplayName("CreateOrders - 재고 부족으로 주문 생성 실패 (Fail)")
    @Test
    void testCreateOrdersWithInsufficientStock() {
        // Given
        List<OrderRequestDto> orderRequestDtos = List.of(
                new OrderRequestDto(1L, 200L, customer.getCustomerId(), "validToken")
        );
        when(jwtTokenProvider.validateToken(anyString())).thenReturn(true);
        when(productRepository.findByIdWithPessimisticLock(anyLong())).thenReturn(Optional.of(product));
        when(customerRepository.findByCustomerId(anyString())).thenReturn(Optional.of(customer));

        // When & Then
        assertThrows(ServiceException.class, () -> orderService.createOrders(orderRequestDtos));
    }

    @DisplayName("GetOrder - 주문 조회 성공 (Success)")
    @Test
    void testGetOrderSuccess() {
        // Given
        Long orderId = 1L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // When
        OrderResponseDto response = orderService.getOrder(orderId);

        // Then
        assertEquals(order.getProductId(), response.getProductId());
    }

    @DisplayName("GetOrder - 존재하지 않는 주문 ID 조회 실패 (Fail)")
    @Test
    void testGetOrderNotFound() {
        // Given
        Long orderId = 999L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ServiceException.class, () -> orderService.getOrder(orderId));
    }

    @DisplayName("UpdateOrder - 정상적으로 주문 수정 (Success)")
    @Test
    void testUpdateOrderSuccess() {
        // Given
        Long orderId = 1L;
        OrderRequestDto requestDto = new OrderRequestDto(1L, 10L, customer.getCustomerId(), "validToken");

        // Mock 설정
        when(jwtTokenProvider.validateToken(anyString())).thenReturn(true);
        when(jwtTokenProvider.getCustomerIdFromToken(anyString())).thenReturn(customer.getCustomerId()); // 토큰에서 customerId 추출
        when(customerRepository.findByCustomerId(customer.getCustomerId())).thenReturn(Optional.of(customer)); // 고객 조회
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(productRepository.findByIdWithPessimisticLock(order.getProductId())).thenReturn(Optional.of(product));

        // When
        OrderResponseDto response = orderService.updateOrder(orderId, requestDto);

        // Then
        assertEquals(10L, response.getQuantity());
    }

    @DisplayName("UpdateOrder - 존재하지 않는 주문 ID로 수정 실패 (Fail)")
    @Test
    void testUpdateOrderNotFound() {
        // Given
        Long orderId = 999L;
        OrderRequestDto requestDto = new OrderRequestDto(1L, 10L, customer.getCustomerId(), "validToken");
        when(jwtTokenProvider.validateToken(anyString())).thenReturn(true);
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ServiceException.class, () -> orderService.updateOrder(orderId, requestDto));
    }

    @DisplayName("CancelOrder - 주문 취소 후 상태 확인 (Success)")
    @Test
    void testCancelOrderSuccess() {
        // Given
        Long orderId = 1L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(productRepository.findByIdWithPessimisticLock(order.getProductId())).thenReturn(Optional.of(product));

        // When
        orderService.cancelOrder(orderId);

        // Then
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(orderRepository, times(1)).save(order);
    }

    @DisplayName("CancelOrder - 존재하지 않는 주문 ID로 취소 실패 (Fail)")
    @Test
    void testCancelOrderNotFound() {
        // Given
        Long orderId = 999L;
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ServiceException.class, () -> orderService.cancelOrder(orderId));
    }

    @DisplayName("GetOrdersWithConditions - 조건에 맞는 주문 목록 조회 (Success)")
    @Test
    void testGetOrdersWithConditionsSuccess() {
        // Given
        OrderConditionDto conditionDto = new OrderConditionDto(customer.getCustomerId(), LocalDate.now().minusDays(30), LocalDate.now(), OrderStatus.PENDING);
        List<Orders> mockOrders = List.of(order);
        when(orderRepository.findOrdersWithConditions(anyString(), any(LocalDate.class), any(LocalDate.class), any(OrderStatus.class)))
                .thenReturn(mockOrders);

        // When
        List<OrderResponseDto> responses = orderService.getOrdersWithConditions(conditionDto);

        // Then
        assertEquals(1, responses.size());
        assertEquals(order.getProductId(), responses.get(0).getProductId());
    }

    @DisplayName("GetOrdersWithConditions - 조건에 맞는 주문이 없을 때 예외 발생 (Fail)")
    @Test
    void testGetOrdersWithConditionsNoMatch() {
        // Given
        OrderConditionDto conditionDto = new OrderConditionDto("nonexistentId", LocalDate.now().minusDays(30), LocalDate.now(), OrderStatus.PENDING);
        when(orderRepository.findOrdersWithConditions(anyString(), any(LocalDate.class), any(LocalDate.class), any(OrderStatus.class)))
                .thenReturn(List.of());

        // When & Then
        assertThrows(ServiceException.class, () -> orderService.getOrdersWithConditions(conditionDto));

    }

    @DisplayName("GetOrdersWithConditions - 상태 및 날짜 필터로 조회 (Success)")
    @Test
    void testGetOrdersWithConditionsWithStatusAndDate() {
        // Given
        OrderConditionDto conditionDto = new OrderConditionDto(customer.getCustomerId(), LocalDate.now().minusDays(10), LocalDate.now(), OrderStatus.SHIPPING);

        Orders shippedOrder = new Orders(product, 1L, customer, OrderStatus.SHIPPING, LocalDateTime.now());

        when(orderRepository.findOrdersWithConditions(
                eq(customer.getCustomerId()),
                any(LocalDate.class),
                any(LocalDate.class),
                eq(OrderStatus.SHIPPING)
        )).thenReturn(List.of(shippedOrder));

        // When
        List<OrderResponseDto> responses = orderService.getOrdersWithConditions(conditionDto);

        // Then
        assertFalse(responses.isEmpty());
        assertTrue(responses.stream().allMatch(r -> OrderStatus.SHIPPING.equals(r.getStatus())));
    }
}