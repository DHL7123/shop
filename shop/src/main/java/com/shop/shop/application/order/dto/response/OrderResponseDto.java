package com.shop.shop.application.order.dto.response;

import com.shop.shop.infrastructure.persistence.member.Customer;
import com.shop.shop.infrastructure.persistence.order.Orders;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDto {

    private Integer orderId;
    private Integer productId;
    private Integer quantity;
    private Customer customer;

    public OrderResponseDto(Orders order) {
        this.orderId = order.getPk();
        this.productId = order.getProductId();
        this.quantity = order.getQuantity();
        this.customer = order.getCustomer();
    }
}


