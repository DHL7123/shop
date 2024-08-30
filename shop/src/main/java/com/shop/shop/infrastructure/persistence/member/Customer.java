package com.shop.shop.infrastructure.persistence.member;

import  com.shop.shop.application.member.dto.request.SignupRequestDto;
import com.shop.shop.infrastructure.persistence.order.Orders;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static  com.shop.shop.infrastructure.constant.StatusConstants.ACTIVE;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer pk; // primary key
    @Column
    private String accountCode; // 계정 코드
    @Column
    private String customerId; // 사용자 아이디
    @Column
    private String password; // 사용자 패스워드
    @Column
    private String userName; // 이름
    @Column
    private String email; // 이메일
    @Column
    private String phoneNumber; // 전화번호
    @Column
    private String address; // 주소 (성암로 55)
    @Column
    private String city; // 도시 (서울특별시)
    @Column
    private String state; // 주/도 (마포구)
    @Column
    private String country; // 국가 (대한민국)
    @Column
    private String zipCode; // 우편번호 (12345)
    @Column
    private String status; // ACTIVE (DELETE, INACTIVE, SUSPENDED, MEMBERSHIP, ...)
    @Column
    private String remarks; // 메모
    @Column
    private LocalDateTime signupDate; // 가입 일시
    @Column
    private LocalDateTime lastLoginDate; // 최종 로그인 일시
    @Column
    private LocalDateTime statusChangedDate; // 상태 변경 일시

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Orders> orders = new ArrayList<>();


    public static Customer initializeCustomer(SignupRequestDto request, String encryptedPassword) {
        return new Customer(request, encryptedPassword);
    }
    public void checkLastLoginTime() {
        this.lastLoginDate = LocalDateTime.now();
    }

    private Customer(SignupRequestDto request, String encryptedPassword) {
        this.status = ACTIVE;
        this.accountCode = "";
        this.customerId = request.getCustomerId();
        this.password = encryptedPassword;
        this.userName = request.getUserName();
        this.email = request.getEmail();
        this.phoneNumber = request.getPhoneNumber();
    }

    // more..
}
