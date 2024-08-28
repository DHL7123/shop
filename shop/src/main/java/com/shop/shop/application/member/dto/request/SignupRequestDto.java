package com.shop.shop.application.member.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignupRequestDto {
    private String customerId;
    private String password;
    private String userName;
    private String email;
    private String phoneNumber;
}
