package com.shop.shop.infrastructure.authentication;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthenticationBuilder {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    // UsernamePasswordAuthenticationToken -> AbstractAuthenticationToken -> Authentication
    // 결국 UsernamePasswordAuthenticationToken 은 Spring SecurityContext 에 등록될 권한(Authentication) 객체
    // 권한 객체를 발급하기 위한 여러가지 방식이 있는데, 기본적으로 로그인은 UsernamePasswordAuthenticationToken 을 발급하고 이걸로 Authentication 객체를 생성

    public Authentication getAuthenticationForLogin(String customerId, String password){
        // UsernamePasswordAuthenticationToken 발급
        // * 이건 SecurityContext 에 등록될 권한 객체 생성을 위한 정보가 담긴 임시 토큰이지, 실제 권한 검사를 수행하는 토큰이 아니다.
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(customerId, password);

        // UsernamePasswordAuthenticationToken 를 통해 Authentication 객체 생성 요청
        // * 아래 authenticate() 메서드를 통해 권한 객체를 생성할 때 순서는
        // * authenticate() 메서드 호출을 통해 AuthenticationProvider 클래스를 호출
        // * AuthenticationProvider 는 사용자 권한 확인 및 토큰 정보에 등록될 유저 정보 생성
        // (유저 정보라고 해서 Customer 의 객체 생성하는게 아닌 권한 객체에 등록될 별도의 유저 클래스를 생성한다. : User)
        // * 유저 정보 생성을 위해 UserDetailService 의 loadUserByUsername 을 호출한다.

        // AuthenticationProvider 를 통해 생성된 User 클래스로 권한 생성 후 반환 된 상태.
        // 완성된 Authentication 객체를 getAuthenticationForLogin()을 호출했던 CustomerService 클래스로 전달한다.
        return authenticationManagerBuilder.getObject().authenticate(authenticationToken);
    }


}
