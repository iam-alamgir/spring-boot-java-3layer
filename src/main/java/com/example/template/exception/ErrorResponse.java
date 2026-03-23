package com.example.template.exception;

import java.time.OffsetDateTime;
import java.util.Map;

public record ErrorResponse(
        String path,
        String message,
        ErrorCode errorCode,
        OffsetDateTime timestamp,
        Map<String, String> details
) {
}
