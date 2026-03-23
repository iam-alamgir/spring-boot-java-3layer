package com.example.template.service;

import com.example.template.security.dto.LoginRequest;
import com.example.template.security.dto.RefreshTokenRequest;
import com.example.template.security.dto.TokenResponse;
import reactor.core.publisher.Mono;

public interface AuthService {
    Mono<TokenResponse> login(LoginRequest request);

    Mono<TokenResponse> refresh(RefreshTokenRequest request);
}
