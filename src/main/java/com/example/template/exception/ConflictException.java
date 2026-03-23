package com.example.template.exception;

import org.springframework.http.HttpStatus;

public final class ConflictException extends ApiException {
    public ConflictException(String message) {
        super(message, ErrorCode.RESOURCE_CONFLICT, HttpStatus.CONFLICT);
    }
}
