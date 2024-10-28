package com.shop.shop.application.order;

import com.shop.shop.application.order.dto.request.OrderConditionDto;
import com.shop.shop.application.order.dto.request.OrderRequestDto;
import com.shop.shop.application.order.dto.response.OrderResponseDto;
import com.shop.shop.domain.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    //주문 생성
    @PostMapping
    public ResponseEntity<List<OrderResponseDto>> createOrder(@RequestBody List<OrderRequestDto> requestDtos) {
        List<OrderResponseDto> orderResponseDtos = orderService.createOrders(requestDtos);
        return ResponseEntity.ok(orderResponseDtos);
    }

    //주문 조회 (단일)
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getOrder(@PathVariable("id") Long orderId) {
        OrderResponseDto orderResponseDto = orderService.getOrder(orderId);
        return new ResponseEntity<>(orderResponseDto, HttpStatus.OK);
    }

    //주문 조회 (전체)
    @GetMapping
    public ResponseEntity<List<OrderResponseDto>> getAllOrders() {
        List<OrderResponseDto> orders = orderService.getAllOrders();
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }


    // 주문 조회 (조건부)
    @GetMapping("/conditions")
    public ResponseEntity<List<OrderResponseDto>> getOrderByCondition(@RequestBody OrderConditionDto conditionDto) {
        List<OrderResponseDto> orders = orderService.getOrdersWithConditions(conditionDto);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }


    //주문 취소
    @DeleteMapping("/{id}")
    public ResponseEntity<OrderResponseDto> deleteOrder(@PathVariable("id") Long orderId) {
        orderService.cancelOrder(orderId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    //주문 수정
    @PutMapping("/{id}")
    public ResponseEntity<OrderResponseDto> updateOrder(@PathVariable("id") Long orderId, @RequestBody OrderRequestDto orderRequestDto) {
        OrderResponseDto updatedOrder = orderService.updateOrder(orderId, orderRequestDto);
        return new ResponseEntity<>(updatedOrder, HttpStatus.OK);
    }
}