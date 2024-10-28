package com.shop.shop.domain.member;

import com.shop.shop.application.member.dto.request.LoginRequestDto;
import com.shop.shop.application.member.dto.request.SignupRequestDto;
import com.shop.shop.application.member.dto.response.LoginResponseDto;
import com.shop.shop.application.member.dto.response.SignupResponseDto;
import com.shop.shop.domain.member.implement.CustomerServiceImpl;
import com.shop.shop.infrastructure.authentication.AuthenticationBuilder;
import com.shop.shop.infrastructure.authentication.JwtTokenProvider;
import com.shop.shop.infrastructure.persistence.member.Customer;
import com.shop.shop.infrastructure.persistence.member.CustomerRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider; // JWT 토큰 생명주기 관리 클래스

    @Mock
    private AuthenticationBuilder authenticationBuilder; // 권한(authentication) 생성 클래스

    @Mock
    private PasswordEncoder passwordEncoder; // 패스워드 암호화 클래스

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer customer;
    private LoginRequestDto  loginRequestDto;
    private SignupRequestDto signupRequestDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        loginRequestDto = new LoginRequestDto("customerId", "password");
        signupRequestDto = new SignupRequestDto("customerId", "password", "userName", "email@example.com","01012345678");
        customer = new Customer();
        customer.setCustomerId("customerId");
        customer.setStatus("ACTIVE");
    }

    @DisplayName("Login - 정상적으로 로그인 성공 (Success)")
    @Test
    void testLogin_Success() {
        //Given
        when(customerRepository.findByCustomerIdAndStatus("customerId", "ACTIVE")).thenReturn(Optional.of(customer));
        when(authenticationBuilder.getAuthenticationForLogin("customerId", "password")).thenReturn(mock(Authentication.class));
        when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn("generatedToken");

        //When
        LoginResponseDto response = customerService.login(loginRequestDto);

        //Then
        assertEquals("generatedToken", response.getToken());
        assertEquals("SUCCESS", response.getStatus());
        verify(customerRepository, times(1)).findByCustomerIdAndStatus("customerId", "ACTIVE");
    }
    @Test
    @DisplayName("Login - 존재하지 않는 사용자로 인한 로그인 실패 (Customer Not Found)")
    void testLogin_CustomerNotFound() {
        //Given
        when(customerRepository.findByCustomerIdAndStatus("customerId", "ACTIVE")).thenReturn(Optional.empty());

        //When
        LoginResponseDto response = customerService.login(loginRequestDto);

        //Then
        assertEquals("NOT_EXIST_CUSTOMER_ACCOUNT", response.getStatus());
        assertEquals("", response.getToken());
    }

    @Test
    @DisplayName("Signup - 정상적으로 회원가입 성공 (Success)")
    void testSignup_Success() {
        //Given
        when(customerRepository.existsByCustomerId("customerId")).thenReturn(false);
        when(customerRepository.existsByEmail("email@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encryptedPassword");

        //When
        SignupResponseDto response = customerService.signup(signupRequestDto);

        //Then
        assertEquals("SUCCESS", response.getResult());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    @DisplayName("Signup - 이미 존재하는 사용자로 인한 회원가입 실패 (Customer Already Exists)")
    void testSignup_CustomerAlreadyExists() {
        //Given
        when(customerRepository.existsByCustomerId("customerId")).thenReturn(true);

        //When
        SignupResponseDto response = customerService.signup(signupRequestDto);

        //Then
        assertEquals("ALREADY_EXIST_DATA", response.getResult());
        verify(customerRepository, never()).save(any(Customer.class));
    }
}