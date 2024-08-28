package com.shop.shop.infrastructure.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ExceptionList {
    BAD_REQUEST(HttpStatus.BAD_REQUEST,"잘못된 요청입니다.", "BAD_REQUEST"),
    NOT_EXIST_DATA(HttpStatus.NOT_FOUND,"요청한 데이터가 존재하지 않습니다.", "NOT_EXIST_DATA"),
    NOT_EXIST_CUSTOMER_ACCOUNT(HttpStatus.BAD_REQUEST,"존재하지 않는 계정입니다.", "NOT_EXIST_CUSTOMER_ACCOUNT"),
    ALREADY_EXIST_DATA(HttpStatus.CONFLICT,"이미 존재하는 데이터입니다.", "ALREADY_EXIST_DATA"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED ,"접근 권한이 없습니다.", "UNAUTHORIZED"),
    UNSUPPORTED_TOKEN(HttpStatus.PROXY_AUTHENTICATION_REQUIRED ,"지원되지 않는 토큰입니다.", "UNSUPPORTED_TOKEN"),
    COMMUNICATION_FAULT(HttpStatus.SERVICE_UNAVAILABLE ,"API 통신에 실패했습니다.", "COMMUNICATION_FAULT"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR ," 서버 에러", "INTERNAL_SERVER_ERROR");

    private final HttpStatus httpStatus;
    private final String message;
    private final String code;
}