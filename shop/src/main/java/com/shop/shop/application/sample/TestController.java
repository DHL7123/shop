package com.shop.shop.application.sample;

import  com.shop.shop.infrastructure.authentication.CustomUser;
import  com.shop.shop.infrastructure.authentication.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TestController {
    // * @AuthenticationPrincipal 을 통해 SecurityContext 에 담긴 정보를 가져올 수 있다.
    @GetMapping("/test")
    public ResponseEntity<String> test(@AuthenticationPrincipal CustomUser user) {
        String customerId = user.getUsername();
        String password = user.getPassword();
        String authority = String.valueOf(user.getAuthorities());

        log.info("사용자 아이디 : {}", customerId);
        log.info("사용자 비밀번호 : {}", password);
        log.info("사용자 권한 : {}", authority);

        return ResponseEntity.ok("");
    }

    // * 유효한 토큰 없이 /api/.. API 호출 시 403 FORBIDDEN 에러 발생
    @GetMapping("/api/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("PASS");
    }
}

