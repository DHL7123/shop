package com.shop.shop.domain.member;

import com.shop.shop.application.member.dto.request.LoginRequestDto;
import com.shop.shop.application.member.dto.request.SignupRequestDto;
import com.shop.shop.application.member.dto.response.LoginResponseDto;
import com.shop.shop.application.member.dto.response.SignupResponseDto;

public interface CustomerService {
    // SEQ 2 : 인터페이스 -> 구현체 호출
    LoginResponseDto login(LoginRequestDto loginRequestDto);
    SignupResponseDto signup(SignupRequestDto request);

}
