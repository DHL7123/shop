package com.shop.shop.domain.member;

import com.shop.shop.application.member.dto.request.LoginRequestDto;
import com.shop.shop.application.member.dto.request.SignupRequestDto;
import com.shop.shop.application.member.dto.response.LoginResponseDto;
import com.shop.shop.application.member.dto.response.SignupResponseDto;

public interface CustomerService {
    LoginResponseDto login(LoginRequestDto loginRequestDto);
    SignupResponseDto signup(SignupRequestDto request);
}
