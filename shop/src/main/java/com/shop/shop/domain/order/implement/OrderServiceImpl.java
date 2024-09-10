package com.shop.shop.domain.order.implement;

import com.shop.shop.application.order.dto.request.OrderRequestDto;
import com.shop.shop.application.order.dto.response.OrderResponseDto;
import com.shop.shop.domain.order.OrderService;
import com.shop.shop.infrastructure.exception.ServiceException;
import com.shop.shop.infrastructure.persistence.member.Customer;
import com.shop.shop.infrastructure.persistence.member.CustomerRepository;
import com.shop.shop.infrastructure.persistence.order.OrderRepository;
import com.shop.shop.infrastructure.persistence.order.Orders;
import com.shop.shop.infrastructure.persistence.product.Product;
import com.shop.shop.infrastructure.persistence.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;


    @Transactional // 주문이 한 번에 여러개 들어온다면? 그 중 하나 DB 접속 실패하면 전부 다 깨지는데 이거 어케 처리할거임?
    @Override
    public OrderResponseDto createOrder(OrderRequestDto requestDto) {

        Product product = productRepository.findById(requestDto.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        product.decreaseStockQuantity(requestDto.getQuantity());
        productRepository.save(product);

        // customerPk를 사용해 Customer 엔티티 조회
        Customer customer = customerRepository.findById(requestDto.getCustomerPk())
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
            // 유효성 검사 -> ServiceException 처리
//        try {
//            Orders order = Orders.create(requestDto.getProductId(),requestDto.getQuantity(), requestDto.getCustomer());
//            orderRepository.save(order);
//        } catch (ServiceException e) {
//            log.info("유효성 검사에 실패하였습니다.");
//        } catch (Exception e) {
//            log.error("DB connection error");
//        }
        // 실제 Orders 엔티티 생성 시 Customer 객체 전달

        Orders order = new Orders(requestDto.getProductId(), requestDto.getQuantity(), customer);
        orderRepository.save(order);

        return new OrderResponseDto(order);
    }
    @Transactional
    @Override
    public void cancelOrder(Long orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        Product product = productRepository.findById(order.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        product.increaseStock(order.getQuantity());  // 재고 증가
        productRepository.save(product);  // 상품 정보 업데이트

        orderRepository.delete(order);  // 주문 삭제
    }
    @Transactional(readOnly = true)
    @Override
    public OrderResponseDto getOrder(Long orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        return new OrderResponseDto(order);
    }
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getAllOrders() { // 디테일 추가 -> 일자별 필터링이 필요하다. 예를들어 year/month 파라미터를 받아서 그 기간 안에있는거만 조회되도록
        List<Orders> orders = orderRepository.findAllWithCustomer(); // -> QueryDsl Projections.constructor 고민해보기 생성자 매핑 가능
        return orders.stream().map(OrderResponseDto::new).collect(Collectors.toList()); // 데이터 주기
    }
    @Transactional
    @Override
    public OrderResponseDto updateOrder(Long orderId, OrderRequestDto requestDto) {

        // 기존 주문
        Orders existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        // 기존 주문 상품
        Product product = productRepository.findById(existingOrder.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        // 기존 주문 수량과 새로운 수량 차이
        Long quantityDiff = existingOrder.getQuantity() - requestDto.getQuantity();

        if (quantityDiff > 0) {
            // 수량이 증가 할 경우
            product.decreaseStockQuantity(quantityDiff);
        }else if(quantityDiff < 0) {
            // 수량이 감소 할 경우
            product.increaseStock(-quantityDiff);
        }
        productRepository.save(product);

        existingOrder.setQuantity(requestDto.getQuantity());
        orderRepository.save(existingOrder);

        return new OrderResponseDto(existingOrder);
    }
}