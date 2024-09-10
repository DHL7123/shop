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
                // * 잘 커스텀해서 쓰면 됨
                .anyRequest().permitAll()
                .and()
                // * cors 필터 추가
                .addFilter(corsConfig.corsFilter())
                // * addFilterBefore(A, B) : B 필터보다 A 필터가 먼저 동작하게 함.
                // * 필터에 관한 상세한 설명은 추가 작성
                // * 요청에 포함되는 JWT 토큰의 검증 필터
                // * 이것도 들어가보세요
                // * JwtFilter 종료 후 UsernamePasswordAuthenticationFilter 동작
                // * UsernamePasswordAuthenticationFilter 는 JwtFilter 에서
                // * SecurityContext 에 UsernamePasswordAuthenticationToken 을 담은 내용을 참조하여 동작
                .addFilterBefore(new JwtFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}

// * 스프링 시큐리티 필터
// * 아래 데이터를 직접 확인하고 싶다면 디버그 모드로 시큐리티를 동작시키면 확인 가능하다. (일단 설정해놓음)

//  Security filter chain: [
//  DisableEncodeUrlFilter
//  -> 세션 아이디가 HTTP Access Log 등에서 누수 될 수 있으므로 URL 외의 정보에 포함되지 않도록 처리한다.
// (기본적인 보안 내용)

//  WebAsyncManagerIntegrationFilter
//  -> Request 에 대한 비동기 Thread 들을 중앙 관리하는 역할을 한다. SecurityContext 를 WebAsyncManager 와 통합하여 비동기 처리 시 파생되는 Thread 에서도 Context 에 접근할 수 있도록 해준다.
//  (멀티스레드 대응 전처리기라는 뜻)

//  SecurityContextPersistenceFilter
//  -> SecurityContext 를 영속화 한다. 별도의 지정이 없다면, HttpSessionSecurityContextRepository 이 사용되며 HttpSession 의 Attribute 에 SecurityContext 가 저장된다.
// (계속 말했던 SecurityContext 초기화하는 내용)

//  HeaderWriterFilter
//  -> Security 관련 헤더를 추가한다. ( 마임 타입 스니핑 방어, XSS 필터, 캐시 히스토리 취약점 방어, 자동 HTTPS 접근, ClickJacking 방어 … )

//  CorsFilter
//  -> CORS 정책이 유효한지 확인하는 필터 / CorsConfiguration 을 토대로 진행

//  LogoutFilter
//  -> Logout 요청이라면 LogoutHandler → LogoutSuccessHandler 순서로 처리 위임

//  JwtFilter
//  -> 설정해놓은 JwtFilter

//  RequestCacheAwareFilter
//  -> 요청을 캐싱 후 쿠키나 세션에 값을 저장하고 응답을 준다. 이 후에 같은 값이 들어오고 그 값이 유효하다면 캐싱 된 요청 값으로 대체해 전달한다.
// (JWT 인증 방식에서의 Cache Aside)

//  SecurityContextHolderAwareRequestFilter
//  -> HttpServletRequest, Response 로 Wrapping Security 관련 서블릿 API 를 제공해준다. ( authenticate() … )
// (그냥 시큐리티 서블릿 API 동작하게 설정해준다는 뜻)

//  AnonymousAuthenticationFilter
//  -> 지금까지 필터를 거치면서 SecurityContext 에 인증된 객체가 존재하지 않으면 익명 인증 토큰 발급.

//  SessionManagementFilter
//  -> 세션 사용 전략을 처리한다. 여기서는 *SessionCreationPolicy*.STATELESS 정책을 사용했다. (새로운 세션을 생성하지도, 기존의 것을 사용하지도 않음)

//  ExceptionTranslationFilter
//  -> 필터체인 내에 발생하는 AccessDeniedException, AuthenticationException 을 처리한다. (인증)

//  FilterSecurityInterceptor
//  -> 사용자가 권한이 있는 페이지에 접속할 때 접속 가능 여부를 판단하는 지점이다. (인가) 권한 처리를 AccessDecisionManager 에게 위임한다.
//]

