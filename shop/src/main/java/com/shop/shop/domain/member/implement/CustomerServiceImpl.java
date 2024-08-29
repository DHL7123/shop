package com.shop.shop.domain.member.implement;

import com.shop.shop.application.member.dto.request.LoginRequestDto;
import com.shop.shop.application.member.dto.request.SignupRequestDto;
import com.shop.shop.application.member.dto.response.LoginResponseDto;
import com.shop.shop.application.member.dto.response.SignupResponseDto;
import com.shop.shop.domain.member.CustomerService;
import com.shop.shop.infrastructure.authentication.AuthenticationBuilder;
import com.shop.shop.infrastructure.authentication.JwtTokenProvider;
import com.shop.shop.infrastructure.exception.ServiceException;
import com.shop.shop.infrastructure.persistence.member.Customer;
import com.shop.shop.infrastructure.persistence.member.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import com.shop.shop.infrastructure.constant.StatusConstants.*;
import com.shop.shop.infrastructure.exception.ExceptionList.*;

import static com.shop.shop.infrastructure.constant.StatusConstants.ACTIVE;
import static com.shop.shop.infrastructure.constant.StatusConstants.SUCCESS;
import static com.shop.shop.infrastructure.exception.ExceptionList.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final JwtTokenProvider jwtTokenProvider; // JWT 토큰 생명주기 관리 클래스
    private final AuthenticationBuilder authenticationBuilder; // 권한(authentication) 생성 클래스
    private final PasswordEncoder passwordEncoder; // 패스워드 암호화 클래스

    // SEQ 3 : 로그인 비즈니스 로직 동작

    @Override
    @Transactional
    public LoginResponseDto login(LoginRequestDto request) {
        String customerId = request.getCustomerId();
        try {
            // SEQ 4 : 요청된 아이디로 DB에 활성화 계정이 있는지 조회 후 없다면 예외 처리 [Optional]
            Customer customer = customerRepository.findByCustomerIdAndStatus(customerId, ACTIVE)
                    .orElseThrow(() -> new ServiceException(NOT_EXIST_CUSTOMER_ACCOUNT));
            // SEQ 5 : 계정이 존재한다면 토큰 생성 요청
            String token = generateToken(customerId, request.getPassword());

            // SEQ 13 : 최종 로그인 시간 등록
            customer.checkLastLoginTime(); // [dirty check 찾아보기]

            // SEQ 14 : 토큰 정보가 담긴 로그인 Response 리턴
            // * 여기서 눈치 챈 것 처럼, 사실 로그인 과정은 토큰을 발급해 SecurityContext 에 저장하는게 전부다.
            // * 로그아웃도 동일하게 SecurityContext 에 저장된 정보를 삭제하는게 끝
            return new LoginResponseDto(customerId, token, SUCCESS);
        } catch (ServiceException e) {
            return new LoginResponseDto(customerId, "", e.getCode());
        } catch (Exception e) {
            return new LoginResponseDto(customerId, "", INTERNAL_SERVER_ERROR.getCode());
        }
    }

    private String generateToken(String customerId, String password) {
        // SEQ 6 : authenticationBuilder 호출을 통해 권한 생성
        Authentication authentication = authenticationBuilder.getAuthenticationForLogin(customerId, password);

        // SEQ 11 : 생성된 권한 객체를 JwtTokenProvider 를 통해 토큰으로 변환 요청 후 리턴한다.
        return jwtTokenProvider.generateToken(authentication);
    }

    @Override
    @Transactional
    public SignupResponseDto signup(SignupRequestDto request) {
        String customerId = request.getCustomerId();
        String email = request.getEmail();
        try {
            if (customerRepository.existsByCustomerId(customerId)) {
                throw new ServiceException(ALREADY_EXIST_DATA);
            }
            if (customerRepository.existsByEmail(email)) {
                throw new ServiceException(ALREADY_EXIST_DATA);
            }
            // * 회원가입은 패스워드를 암호화하는 것 말고는 크게 없다.
            // * 나머지는 디테일
            // * (회원가입 할 때 다양한 데이터를 추가하고 유저 코드도 생성하고 이메일도 보내주고 개인정보 보호정책도 추가하고,, 이런게 디테일이라고 생각하면 됨)
            String encryptedPassword = passwordEncoder.encode(request.getPassword());

            Customer customer = Customer.initializeCustomer(request, encryptedPassword);
            customerRepository.save(customer);

            return new SignupResponseDto(SUCCESS, customerId, email);
        } catch (ServiceException e) {
            return new SignupResponseDto(e.getCode(), customerId, email);
        } catch (Exception e) {
            return new SignupResponseDto(INTERNAL_SERVER_ERROR.getCode(), customerId, email);
        }
    }

}