package com.shop.shop.domain.order;

import com.shop.shop.application.order.dto.request.OrderRequestDto;
import com.shop.shop.application.order.dto.response.OrderResponseDto;
import org.springframework.data.domain.jaxb.SpringDataJaxb;

import java.util.List;

public interface OrderService {

    OrderResponseDto createOrder(OrderRequestDto requestDto);
    void cancelOrder(Integer orderId);
    OrderResponseDto getOrder(Integer orderId);
    List<OrderResponseDto> getAllOrders();
    OrderResponseDto updateOrder(Integer orderId, OrderRequestDto requestDto);
}
