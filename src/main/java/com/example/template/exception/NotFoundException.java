package com.example.template.exception;

import org.springframework.http.HttpStatus;

public final class NotFoundException extends ApiException {
    public NotFoundException(String message) {
        super(message, ErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND);
    }
}
