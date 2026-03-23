package com.example.template.config;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record ApplicationProperties(
        JwtProperties jwt,
        LoggingProperties logging,
        ResilienceProperties resilience
) {
    public record JwtProperties(String issuer, String audience, String secret, Duration accessTokenTtl,
                                Duration refreshTokenTtl) {
    }

    public record LoggingProperties(List<String> sensitiveFields) {
    }

    public record ResilienceProperties(Duration timeout, int retryAttempts, int rateLimitForPeriod,
                                       Duration rateLimitRefreshPeriod) {
    }
}
