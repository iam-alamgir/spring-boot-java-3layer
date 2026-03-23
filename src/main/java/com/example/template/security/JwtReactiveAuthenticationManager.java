package com.example.template.security;

import com.example.template.cache.TokenCacheService;
import java.util.List;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class JwtReactiveAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtTokenService jwtTokenService;
    private final TokenCacheService tokenCacheService;

    public JwtReactiveAuthenticationManager(JwtTokenService jwtTokenService, TokenCacheService tokenCacheService) {
        this.jwtTokenService = jwtTokenService;
        this.tokenCacheService = tokenCacheService;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        var token = authentication.getCredentials().toString();
        return jwtTokenService.verify(token, "access")
                .flatMap(claims -> tokenCacheService.isAccessTokenActive(token)
                        .filter(Boolean::booleanValue)
                        .switchIfEmpty(Mono.error(new com.example.template.exception.AuthenticationException("Access token revoked or expired in cache")))
                        .thenReturn(claims))
                .map(claims -> new UsernamePasswordAuthenticationToken(
                        new AuthenticatedUser(claims.userId(), claims.username()), token, List.of()));
    }
}
