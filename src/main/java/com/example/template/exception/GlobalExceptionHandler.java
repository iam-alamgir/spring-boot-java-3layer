package com.example.template.exception;

import java.time.OffsetDateTime;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger APPLICATION_LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final Logger AUDIT_LOG = LoggerFactory.getLogger("AUDIT_LOG");

    @ExceptionHandler(ApiException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleApiException(ApiException exception, ServerWebExchange exchange) {
        var body = new ErrorResponse(exchange.getRequest().getPath().value(), exception.getMessage(),
                exception.errorCode(), OffsetDateTime.now(), Map.of());
        routeLog(exception);
        return Mono.just(ResponseEntity.status(exception.status()).body(body));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidation(WebExchangeBindException exception, ServerWebExchange exchange) {
        var details = exception.getFieldErrors().stream()
                .collect(java.util.stream.Collectors.toMap(fieldError -> fieldError.getField(),
                        fieldError -> String.valueOf(fieldError.getDefaultMessage()), (left, right) -> left));
        var body = new ErrorResponse(exchange.getRequest().getPath().value(), "Validation failed",
                ErrorCode.VALIDATION_ERROR, OffsetDateTime.now(), details);
        APPLICATION_LOG.warn("validation-error path={} details={}", exchange.getRequest().getPath(), details);
        return Mono.just(ResponseEntity.badRequest().body(body));
    }

    @ExceptionHandler({DecodingException.class, IllegalArgumentException.class})
    public Mono<ResponseEntity<ErrorResponse>> handleBadRequest(Exception exception, ServerWebExchange exchange) {
        var body = new ErrorResponse(exchange.getRequest().getPath().value(), exception.getMessage(),
                ErrorCode.VALIDATION_ERROR, OffsetDateTime.now(), Map.of());
        APPLICATION_LOG.warn("bad-request path={} message={}", exchange.getRequest().getPath(), exception.getMessage());
        return Mono.just(ResponseEntity.badRequest().body(body));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleUnknown(Exception exception, ServerWebExchange exchange) {
        APPLICATION_LOG.error("unexpected-error path={}", exchange.getRequest().getPath(), exception);
        var body = new ErrorResponse(exchange.getRequest().getPath().value(), "Internal server error",
                ErrorCode.INTERNAL_ERROR, OffsetDateTime.now(), Map.of());
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body));
    }

    private void routeLog(ApiException exception) {
        switch (exception) {
            case AuthenticationException auth -> AUDIT_LOG.warn("authentication-failure reason={}", auth.getMessage());
            case AuthorizationException authorization -> AUDIT_LOG.warn("access-denied reason={}", authorization.getMessage());
            case ConflictException conflict -> APPLICATION_LOG.warn("business-conflict reason={}", conflict.getMessage());
            case NotFoundException notFound -> APPLICATION_LOG.info("resource-not-found reason={}", notFound.getMessage());
        }
    }
}
