package com.example.template.security;

import java.time.OffsetDateTime;
import java.util.UUID;

public record JwtClaims(UUID userId, String username, String tokenType, OffsetDateTime expiresAt) {
}
