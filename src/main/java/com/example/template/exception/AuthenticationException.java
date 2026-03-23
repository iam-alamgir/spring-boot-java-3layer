package com.example.template.exception;

import org.springframework.http.HttpStatus;

public final class AuthenticationException extends ApiException {
    public AuthenticationException(String message) {
        super(message, ErrorCode.AUTHENTICATION_FAILED, HttpStatus.UNAUTHORIZED);
    }
}
