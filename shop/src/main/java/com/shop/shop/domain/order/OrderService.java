package com.shop.shop.domain.order;

import com.shop.shop.application.order.dto.request.OrderConditionDto;
import com.shop.shop.application.order.dto.request.OrderRequestDto;
import com.shop.shop.application.order.dto.response.OrderResponseDto;
import com.shop.shop.infrastructure.constant.OrderStatus;
import org.springframework.data.domain.jaxb.SpringDataJaxb;

import java.time.LocalDate;
import java.util.List;

public interface OrderService {

    OrderResponseDto createOrder(OrderRequestDto requestDto);
    void cancelOrder(Long orderId);
    OrderResponseDto getOrder(Long orderId);
    List<OrderResponseDto> getAllOrders();
    OrderResponseDto updateOrder(Long orderId, OrderRequestDto requestDto);
    List<OrderResponseDto> getOrdersWithConditions(OrderConditionDto conditionDto);
    List<OrderResponseDto> createMultipleOrders(List<OrderRequestDto> orderRequestDto);
}
