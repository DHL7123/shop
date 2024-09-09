package com.shop.shop.application.order.dto.request;

import com.shop.shop.infrastructure.persistence.member.Customer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequestDto {

    private Integer productId;
    private Integer quantity;
    private Customer customer;

}
