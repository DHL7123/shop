package com.shop.shop.infrastructure.authentication;

import  com.shop.shop.infrastructure.exception.ExceptionList;
import  com.shop.shop.infrastructure.exception.ServiceException;
import  com.shop.shop.infrastructure.persistence.member.Customer;
import  com.shop.shop.infrastructure.persistence.member.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collections;

import static  com.shop.shop.infrastructure.constant.StatusConstants.ACTIVE;
import static  com.shop.shop.infrastructure.exception.ExceptionList.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;
    private static final String ROLE_CUSTOMER = "ROLE_CUSTOMER";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    // SEQ 9 : loadUserByUsername 을 통해 권한이 부여된 UserDetail 클래스를 생성 후 그 정보를 통해 User 객체를 생성해 반환한다.
    // * 여기서 유저는 Spring SecurityContext 에 등록될 권한이 포함된 클래스이다.
    // * 이 지점에서 로그인 되는 사용자의 권한이 지정된다.

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String customerId) throws UsernameNotFoundException {
        return customerRepository.findByCustomerId(customerId)
                .map(this::createUserDetails)
                .orElseThrow(() -> new ServiceException(NOT_EXIST_CUSTOMER_ACCOUNT));
    }
    private User createUserDetails(Customer customer) {
        // * 엣지케이스 방지를 위한 더블 체크
        if (!customer.getStatus().equals(ACTIVE)) {
            throw new ServiceException(UNAUTHORIZED);
        }
        // * 기본적으로 일반 권한을 부여한다.
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(ROLE_CUSTOMER);
        // * 특정 플래그가 존재할 경우 어드민 권한 부여
        if (customer.getAccountCode().equals("ADMIN FLAG")){
            grantedAuthority = new SimpleGrantedAuthority(ROLE_ADMIN);
        }
        return new User(customer.getCustomerId(), customer.getPassword(), Collections.singleton(grantedAuthority));
    }
}