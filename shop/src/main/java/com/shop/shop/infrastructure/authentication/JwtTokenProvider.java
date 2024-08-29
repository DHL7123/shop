package com.shop.shop.infrastructure.authentication;

import  com.shop.shop.infrastructure.exception.ServiceException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import static  com.shop.shop.infrastructure.exception.ExceptionList.*;

@Slf4j
@Component
public class JwtTokenProvider {

    private static final String AUTHORITIES_KEY = "auth";
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 10 * 60 * 1000;
    private final Key key;

    public JwtTokenProvider(@Value("${jwt.security.key}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Authentication authentication) {
        // * 권한 객체에 등록되어 있던 사용자의 권한을 가져온다. (ROLE_CUSTOMER, ROLE_ADMIN, ...)
        // * 권한이 여러개 존재할 수 있으니 구분해준다. (이건 List 로 처리해도 되고, 가장 높은 권한 하나만 전달해도 무방)
        // * 나중에 토큰을 복호화해 프론트에서 지정된 값을 사용하는데, 사실 여기 담기는 내용은 그쪽에서 요청하는 값으로 전달해주면 되니 취향껏 하면 된다.
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // * 토큰 만료기간 설정
        long now = (new Date()).getTime();
        Date accessTokenExpiresIn = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);

        // * accessToken = JWT 토큰의 초기 발급 네이밍, 이후 갱신되는 토큰은 refreshToken 으로 명칭한다.
        // * setSubject, 토큰에 지정될 제목. 권한 객체를 만들 당시 customerId 로 등록되었음 (아마..)
        // * claim, 토큰에 저장될 데이터 블록, 여러개 포함 가능하니 그냥 포함되는 정보 블록 정도로 생각하면 된다. 여기선 권한 등록
        // * setExpiration, 토큰의 만료기간 설정
        // * signWith, JWT 토큰 암호화 키와 알고리즘을 지정한다.
        // * compact, 합체
        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim(AUTHORITIES_KEY, authorities)
                .setExpiration(accessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }


    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);

        // * claims : 토큰의 데이터 조각
        // * 위에서 저장된 권한 데이터를 가져오고, 만약 권한이 없는 사용자라면 에러 리턴
        if (claims.get(AUTHORITIES_KEY) == null) {
            throw new ServiceException(UNAUTHORIZED);
        }

        // * 권한 문자열을 리스트로 반환
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        // * 이전에 UsernamePasswordAuthenticationToken -> Authentication -> JWT 순서로 만들었음
        // * 이번엔 역순으로 위 저장된 데이터를 통해 UsernamePasswordAuthenticationToken 반환
        // * UsernamePasswordAuthenticationToken : 사용자의 아이디, 패스워드, 권한이 담긴 객체

        // * UsernamePasswordAuthenticationToken 는 SecurityContext 에 저장되어 추후 권한에 검증에 사용된다.
        // * 여기서 왜 로그인 할 때 SecurityContext 안에 권한을 담지 않았지? 라고 생각할 수 있으나,
        // * 로그인 호출을 통해 토큰 발급 -> 토큰 정보를 담아 메인페이지 콜백 호출 순서로 이해하면 된다.

        UserDetails principal = new CustomUser(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    public boolean validateToken(String accessToken) {
        try {
            // * parserBuilder() JWT 토큰 파싱을 통해 유효한 토큰인지 체크
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken);
            return true;
            // * 만약 토큰이 사용 불가 상태일 경우 아래 Exception 발생
        } catch (SecurityException | MalformedJwtException | IllegalArgumentException |
                 UnsupportedJwtException | ExpiredJwtException e) {
            throw new ServiceException(UNSUPPORTED_TOKEN);
        }
    }

    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}