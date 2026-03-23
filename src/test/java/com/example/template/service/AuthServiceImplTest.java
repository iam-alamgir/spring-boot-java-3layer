package com.example.template.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.template.config.ApplicationProperties;
import com.example.template.cache.TokenCacheService;
import com.example.template.repository.RefreshTokenRepository;
import com.example.template.repository.UserRepository;
import com.example.template.repository.entity.RefreshTokenEntity;
import com.example.template.repository.entity.UserEntity;
import com.example.template.security.JwtTokenService;
import com.example.template.security.dto.LoginRequest;
import com.example.template.service.impl.AuthServiceImpl;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private TokenCacheService tokenCacheService;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        var encoder = new BCryptPasswordEncoder();
        var tokenService = new JwtTokenService(new ApplicationProperties(
                new ApplicationProperties.JwtProperties("issuer", "audience", "12345678901234567890123456789012",
                        Duration.ofMinutes(15), Duration.ofDays(7)),
                new ApplicationProperties.LoggingProperties(List.of("password")),
                new ApplicationProperties.ResilienceProperties(Duration.ofSeconds(2), 3, 20, Duration.ofSeconds(1)),
                new ApplicationProperties.CacheProperties(Duration.ofMinutes(15), Duration.ofDays(7))
        ));
        authService = new AuthServiceImpl(userRepository, refreshTokenRepository, encoder, tokenService, tokenCacheService);
    }

    @Test
    void loginShouldReturnTokenBundle() {
        var password = "secret";
        var user = new UserEntity(UUID.randomUUID(), "demo", new BCryptPasswordEncoder().encode(password),
                "Demo User", true, OffsetDateTime.now(), OffsetDateTime.now());

        when(userRepository.findByUsername("demo")).thenReturn(Mono.just(user));
        when(refreshTokenRepository.findByUserIdAndRevokedFalse(user.id())).thenReturn(Mono.empty());
        when(refreshTokenRepository.save(any(RefreshTokenEntity.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(tokenCacheService.cacheAccessToken(any(), any())).thenReturn(Mono.empty());
        when(tokenCacheService.cacheRefreshToken(any(), any())).thenReturn(Mono.empty());

        StepVerifier.create(authService.login(new LoginRequest("demo", password)))
                .expectNextMatches(response -> response.accessToken() != null && response.refreshToken() != null)
                .verifyComplete();
    }
}
