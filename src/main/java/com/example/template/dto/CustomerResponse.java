package com.example.template.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String externalRef,
        String fullName,
        String email,
        OffsetDateTime createdAt
) {
}
