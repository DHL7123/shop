package com.shop.shop.domain.order.implement;

import com.shop.shop.application.order.dto.request.OrderRequestDto;
import com.shop.shop.application.order.dto.response.OrderResponseDto;
import com.shop.shop.domain.order.OrderService;
import com.shop.shop.infrastructure.persistence.order.OrderRepository;
import com.shop.shop.infrastructure.persistence.order.Orders;
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


    @Transactional
    @Override
    public OrderResponseDto createOrder(OrderRequestDto requestDto) {
        Orders order = new Orders(requestDto.getProductId(),requestDto.getQuantity(), requestDto.getCustomer());
        Orders saveOrder = orderRepository.save(order);
        return new OrderResponseDto(saveOrder);
    }
    @Transactional
    @Override
    public void cancelOrder(Integer orderId) {
        Orders order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Order not found"));
        orderRepository.delete(order);
    }
    @Transactional(readOnly = true)
    @Override
    public OrderResponseDto getOrder(Integer orderId) {
        Orders order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Order not found"));
        return new OrderResponseDto(order);
    }
    @Transactional(readOnly = true)
    @Override
    public List<OrderResponseDto> getAllOrders() {
        List<Orders> orders = orderRepository.findAll();
        return orders.stream().map(OrderResponseDto::new).collect(Collectors.toList());
    }
    @Transactional
    @Override
    public OrderResponseDto updateOrder(Integer orderId, OrderRequestDto requestDto) {
        Orders order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Order not found"));
        order.updateOrder(requestDto.getProductId(), requestDto.getQuantity());
        Orders updatedOrder = orderRepository.save(order);
        return new OrderResponseDto(updatedOrder);
    }
}