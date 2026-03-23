package com.example.template.security;

import java.time.OffsetDateTime;

public record TokenBundle(String accessToken, String refreshToken, OffsetDateTime accessTokenExpiresAt,
                          OffsetDateTime refreshTokenExpiresAt) {
}
