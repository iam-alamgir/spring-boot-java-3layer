package com.example.template.exception;

import org.springframework.http.HttpStatus;

public final class AuthorizationException extends ApiException {
    public AuthorizationException(String message) {
        super(message, ErrorCode.ACCESS_DENIED, HttpStatus.FORBIDDEN);
    }
}
