package com.shop.shop.application.member;

import com.shop.shop.application.member.dto.request.LoginRequestDto;
import com.shop.shop.application.member.dto.request.SignupRequestDto;
import com.shop.shop.application.member.dto.response.LoginResponseDto;
import com.shop.shop.application.member.dto.response.SignupResponseDto;
import com.shop.shop.domain.member.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;

    // TODO : Filter 에 관한 기본 지식 학습 필요 (Filter, Interceptor)

    // SEQ 1 : /login API 를 통해 CustomerService 인터페이스에 로그인 동작 요청

    @PostMapping("/login")
    public ResponseEntity<Object> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        LoginResponseDto data = customerService.login(loginRequestDto);

        return ResponseEntity.ok()
                .header(AUTHORIZATION, data.getToken())
                .body(data.getToken());
    }

    @PostMapping("/signup")
    public ResponseEntity<Object> signup(@RequestBody SignupRequestDto signupRequestDto) {
        SignupResponseDto data = customerService.signup(signupRequestDto);

        return ResponseEntity.ok()
                .body(data.getResult());

    }

}