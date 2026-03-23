package com.example.template.security;

import java.util.List;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class JwtReactiveAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtTokenService jwtTokenService;

    public JwtReactiveAuthenticationManager(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        var token = authentication.getCredentials().toString();
        return jwtTokenService.verify(token, "access")
                .map(claims -> new UsernamePasswordAuthenticationToken(
                        new AuthenticatedUser(claims.userId(), claims.username()), token, List.of()));
    }
}
