package com.example.template.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import java.time.Duration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(ApplicationProperties.class)
public class ApplicationConfig {

    @Bean
    WebClient webClient() {
        return WebClient.builder().build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CircuitBreakerRegistry circuitBreakerRegistry() {
        return CircuitBreakerRegistry.of(CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .slidingWindowSize(20)
                .waitDurationInOpenState(Duration.ofSeconds(15))
                .build());
    }

    @Bean
    RetryRegistry retryRegistry(ApplicationProperties properties) {
        return RetryRegistry.of(RetryConfig.custom()
                .maxAttempts(properties.resilience().retryAttempts())
                .waitDuration(Duration.ofMillis(200))
                .build());
    }

    @Bean
    RateLimiterRegistry rateLimiterRegistry(ApplicationProperties properties) {
        return RateLimiterRegistry.of(RateLimiterConfig.custom()
                .timeoutDuration(Duration.ZERO)
                .limitForPeriod(properties.resilience().rateLimitForPeriod())
                .limitRefreshPeriod(properties.resilience().rateLimitRefreshPeriod())
                .build());
    }

    @Bean
    TimeLimiterRegistry timeLimiterRegistry(ApplicationProperties properties) {
        return TimeLimiterRegistry.of(TimeLimiterConfig.custom()
                .timeoutDuration(properties.resilience().timeout())
                .build());
    }
}
