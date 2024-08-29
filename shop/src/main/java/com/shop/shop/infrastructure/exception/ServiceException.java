package com.shop.shop.infrastructure.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ServiceException extends RuntimeException{
    private final String code;

    public ServiceException(ExceptionList exceptionList) {
        super(exceptionList.getMessage());
        this.code = exceptionList.getCode();
    }
}
