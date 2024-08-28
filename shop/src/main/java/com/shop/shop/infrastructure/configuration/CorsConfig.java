package com.shop.shop.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {
    // * Cors = Cross-Origin Resource Sharing (교차 출처 리소스 공유)
    // * 기본적으로 브라우저는 '동일한 출처(host)에서만 리소스를 공유할 수 있다.' 라는 SOP 정책을 따르고 활성되어 있음
    // * 만약 서버사이드 개발의 경우 프론트엔드 서버 <-> 백엔드 서버에서 데이터가 공유되어야 하는데, 이를 방해함.
    // * 이를 해결하기 위해 '교차 출처 리소스 공유(Cors)' 설정을 함

    // * 아래 소스코드에 addAllowedOriginPattern 을 통해 프론트 서버인 3030을 열어주고,
    // * 해당 서버에 한하여 헤더, 메소드 등 전부 공유 허용 처리함
    // * 추가로 Authorization 헤더를 노출시켰는데, 이는 JWT 토큰이 저장될 경로임

    // * 사실 프론트 작업은 하지 않을거지만, 알아두면 정말 좋은 내용이라 추가해뒀습니다냥~

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("http://localhost:3030");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.addExposedHeader("Authorization");
        source.registerCorsConfiguration("/**",config);
        return new CorsFilter(source);
    }
}