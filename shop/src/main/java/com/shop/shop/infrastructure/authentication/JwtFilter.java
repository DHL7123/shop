package com.shop.shop.infrastructure.authentication;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    // * 필터에 등록된 doFilterInternal(..)을 통해 JWT 토큰 검증이 실행

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        // * HttpServletRequest 에서 토큰 꺼내기
        String token = pickUpToken(request);
        // validateToken()을 통해 토큰이 유효한 상태인지 확인
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            // getAuthentication()을 통해 권한 확인
            Authentication authentication = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }

    private String pickUpToken(HttpServletRequest request) {
        // * 토큰은 Bearer 토큰으로 저장된다.
        // * Bearer 는 소유자라는 뜻인데, "이 토큰의 소유자에게 권한을 부여해줘" 라는 의미로 지어진 이름
        // * 토큰이 여러개 운용되는 환경에서 구분을 위해 지정한 명칭이며 규칙처럼 사용된다.
        // * -> 'Bearer {token..}'

        // * 컨트롤러를 확인하면 Authorization 헤더에 토큰을 저장한 내역이 있다. (토큰 저장의 규칙)
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            // * 정확한 검증을 위해 'Bearer ' 문자열 제거 후 토큰 리턴
            return bearerToken.substring(7);
        }
        return null;
    }
}
