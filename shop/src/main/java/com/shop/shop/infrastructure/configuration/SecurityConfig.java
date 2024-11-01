package com.shop.shop.infrastructure.configuration;

import  com.shop.shop.infrastructure.authentication.JwtFilter;
import  com.shop.shop.infrastructure.authentication.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
// * Spring Security 활성화 및 웹 보안 설정 자동 구성 및 필터에 스프링 시큐리티가 동작되도록 선언하는 어노테이션
// * 필터 동작 확인을 위해 디버그 모드 true 설정
@EnableWebSecurity//(debug = true)
// * @Secured 등 권한 관련 어노테이션 사용 처리를 위한 어노테이션
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {
    private final JwtTokenProvider jwtTokenProvider;
    private final CorsConfig corsConfig;

    // * API 요청 시 동작되는 필터의 체이닝 메소드
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // * CSRF 방어 비활성화
                .csrf().disable()
                // * 응답 헤더 설정. 미설정 시 디폴드 값
                .headers()
                // * XFrameOptionsHeaderWriter 최적화 설정(클랙재킹 방어 설정) 비활성화
                .frameOptions().disable()
                .and()
                .httpBasic().disable()
                // * 기본 제공 로그인 페이지 비활성화 (저번에 너희가 했던 로그인 페이지가 아마 이거일듯)
                .formLogin().disable()
                // * 스프링 시큐리티는 원래 세션 기반으로 동작하는데, 여기선 JWT 사용을 위해 비활성화 처리
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                // * /api/.. 로 들어오는 요청은 전부 권한 확인 처리
                .authorizeRequests().antMatchers("/api/*").authenticated()
                // * 그 외 api 가 없는 요청은 전부 허용
                // * /login 등 권한이 필요 없는 요청만 특정해서 허용 가능
                .anyRequest().permitAll()
                .and()
                // * cors 필터 추가
                .addFilter(corsConfig.corsFilter())
                // * addFilterBefore(A, B) : B 필터보다 A 필터가 먼저 동작하게 함.
                // * 요청에 포함되는 JWT 토큰의 검증 필터
                // * JwtFilter 종료 후 UsernamePasswordAuthenticationFilter 동작
                // * UsernamePasswordAuthenticationFilter 는 JwtFilter 에서
                // * SecurityContext 에 UsernamePasswordAuthenticationToken 을 담은 내용을 참조하여 동작
                .addFilterBefore(new JwtFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}


