package com.example.template.exception;

import org.springframework.http.HttpStatus;

public abstract sealed class ApiException extends RuntimeException permits AuthenticationException,
        AuthorizationException, ConflictException, NotFoundException {

    private final ErrorCode errorCode;
    private final HttpStatus status;

    protected ApiException(String message, ErrorCode errorCode, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }

    public HttpStatus status() {
        return status;
    }
}
