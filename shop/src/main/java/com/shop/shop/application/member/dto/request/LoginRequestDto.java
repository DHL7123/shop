package com.shop.shop.application.member.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequestDto {
    @NotBlank(message = "아이디를 입력해주세요.")
    private String customerId;
    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;
}

// * [Spring-Validation 찾아보기]