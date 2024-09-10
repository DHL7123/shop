package com.shop.shop.application.order;

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
    // Request/Response Log -> AOP : Controller 클래스 한정으로 API 동작 시 Req/Res 로그 작성하게 처리
    //

    //주문 생성
    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(@RequestBody OrderRequestDto requestDto) {
        OrderResponseDto orderResponseDto = orderService.createOrder(requestDto);
//        return new ResponseEntity<>(orderResponseDto, HttpStatus.CREATED);
        return ResponseEntity.ok(orderResponseDto); // 성공은 200
    }
    // 200 OK, 40X, 500 SERVER_ERROR

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

    //주문 취소
    @DeleteMapping("/{id}")
    public ResponseEntity<OrderResponseDto> deleteOrder(@PathVariable("id") Long orderId) {
        orderService.cancelOrder(orderId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    //주문 수정
    @PutMapping({"/id"})
    public ResponseEntity<OrderResponseDto> updateOrder(@PathVariable("id") Long orderId, @RequestBody OrderRequestDto orderRequestDto) {
        OrderResponseDto updatedOrder = orderService.updateOrder(orderId, orderRequestDto);
        return new ResponseEntity<>(updatedOrder, HttpStatus.OK);
    }
}