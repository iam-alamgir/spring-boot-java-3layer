package com.example.template.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

@Configuration
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http,
                                                     JwtReactiveAuthenticationManager authenticationManager,
                                                     AclAuthorizationManager aclAuthorizationManager) {
        var authenticationFilter = new AuthenticationWebFilter(authenticationManager);
        authenticationFilter.setServerAuthenticationConverter(new BearerTokenServerAuthenticationConverter());
        authenticationFilter.setSecurityContextRepository(NoOpServerSecurityContextRepository.getInstance());
        authenticationFilter.setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers("/api/**"));

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .authorizeExchange(exchange -> exchange
                        .pathMatchers("/actuator/health", "/actuator/info", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/v1/auth/login", "/api/v1/auth/refresh").permitAll()
                        .pathMatchers("/api/**").access(aclAuthorizationManager)
                        .anyExchange().authenticated())
                .addFilterAt(authenticationFilter, org.springframework.security.web.server.SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}
