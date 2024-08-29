package com.shop.shop.infrastructure.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static  com.shop.shop.infrastructure.exception.ExceptionList.BAD_REQUEST;


@RestControllerAdvice
@Slf4j
public class ServiceExceptionHandler {
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> except(HttpMessageNotReadableException e) {
        if (e.getRootCause() instanceof ServiceException) {
            return except(
                (ServiceException)e.getRootCause()
            );
        } else {
            return except(
                new ServiceException(BAD_REQUEST)
            );
        }
    }
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Object> except(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity
            .status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(e.getLocalizedMessage());
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> except(Exception e) {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(e.getClass());
    }
}
