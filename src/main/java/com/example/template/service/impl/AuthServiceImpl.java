package com.example.template.service.impl;

import com.example.template.exception.AuthenticationException;
import com.example.template.repository.RefreshTokenRepository;
import com.example.template.repository.UserRepository;
import com.example.template.repository.entity.RefreshTokenEntity;
import com.example.template.security.JwtTokenService;
import com.example.template.security.dto.LoginRequest;
import com.example.template.security.dto.RefreshTokenRequest;
import com.example.template.security.dto.TokenResponse;
import com.example.template.service.AuthService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthServiceImpl(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository,
                           PasswordEncoder passwordEncoder, JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public Mono<TokenResponse> login(LoginRequest request) {
        return userRepository.findByUsername(request.username())
                .switchIfEmpty(Mono.error(new AuthenticationException("Invalid credentials")))
                .filter(user -> user.enabled())
                .switchIfEmpty(Mono.error(new AuthenticationException("User is disabled")))
                .filter(user -> passwordEncoder.matches(request.password(), user.passwordHash()))
                .switchIfEmpty(Mono.error(new AuthenticationException("Invalid credentials")))
                .flatMap(user -> jwtTokenService.issueTokens(user.id(), user.username())
                        .flatMap(bundle -> refreshTokenRepository.findByUserIdAndRevokedFalse(user.id())
                                .flatMap(existing -> refreshTokenRepository.save(new RefreshTokenEntity(
                                        existing.id(), existing.userId(), existing.tokenHash(), existing.expiresAt(), true,
                                        existing.createdAt())))
                                .onErrorResume(ex -> Mono.empty())
                                .then(refreshTokenRepository.save(new RefreshTokenEntity(
                                        UUID.randomUUID(),
                                        user.id(),
                                        sha256(bundle.refreshToken()),
                                        bundle.refreshTokenExpiresAt(),
                                        false,
                                        OffsetDateTime.now(ZoneOffset.UTC)
                                )))
                                .thenReturn(new TokenResponse(bundle.accessToken(), bundle.refreshToken(), "Bearer",
                                        bundle.accessTokenExpiresAt(), bundle.refreshTokenExpiresAt()))));
    }

    @Override
    public Mono<TokenResponse> refresh(RefreshTokenRequest request) {
        return jwtTokenService.verify(request.refreshToken(), "refresh")
                .flatMap(claims -> refreshTokenRepository.findByUserIdAndRevokedFalse(claims.userId())
                        .switchIfEmpty(Mono.error(new AuthenticationException("Refresh token not found")))
                        .filter(token -> token.expiresAt().isAfter(OffsetDateTime.now(ZoneOffset.UTC)))
                        .filter(token -> token.tokenHash().equals(sha256(request.refreshToken())))
                        .switchIfEmpty(Mono.error(new AuthenticationException("Refresh token invalid or expired")))
                        .flatMap(token -> refreshTokenRepository.save(new RefreshTokenEntity(
                                token.id(), token.userId(), token.tokenHash(), token.expiresAt(), true, token.createdAt()))
                                .then(jwtTokenService.issueTokens(claims.userId(), claims.username()))
                                .flatMap(bundle -> refreshTokenRepository.save(new RefreshTokenEntity(
                                        UUID.randomUUID(), claims.userId(), sha256(bundle.refreshToken()),
                                        bundle.refreshTokenExpiresAt(), false, OffsetDateTime.now(ZoneOffset.UTC)))
                                        .thenReturn(new TokenResponse(bundle.accessToken(), bundle.refreshToken(), "Bearer",
                                                bundle.accessTokenExpiresAt(), bundle.refreshTokenExpiresAt())))));
    }

    private String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", exception);
        }
    }
}
