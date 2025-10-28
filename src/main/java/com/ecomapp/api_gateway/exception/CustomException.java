package com.ecomapp.api_gateway.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomException extends RuntimeException {
    private HttpStatus status;
    public CustomException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
