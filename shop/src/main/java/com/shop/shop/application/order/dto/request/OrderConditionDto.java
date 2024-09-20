package com.shop.shop.application.order.dto.request;

import com.shop.shop.infrastructure.constant.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderConditionDto {

    @NotBlank(message = "아이디를 입력해주세요.")
    private String customerId;

    private LocalDate startDate;
    private LocalDate endDate;
    private OrderStatus status;
}
