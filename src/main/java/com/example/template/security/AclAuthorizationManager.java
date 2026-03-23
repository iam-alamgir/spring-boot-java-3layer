package com.example.template.security;

import com.example.template.acl.AclService;
import com.example.template.acl.PermissionTarget;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AclAuthorizationManager implements ReactiveAuthorizationManager<ServerWebExchange> {

    private final AclService aclService;

    public AclAuthorizationManager(AclService aclService) {
        this.aclService = aclService;
    }

    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> authentication, ServerWebExchange exchange) {
        var controller = exchange.getAttributeOrDefault("acl.controller", "UNKNOWN");
        var endpoint = exchange.getAttributeOrDefault("acl.endpoint", exchange.getRequest().getPath().value());
        var action = exchange.getAttributeOrDefault("acl.action", exchange.getRequest().getMethod().name());
        return authentication
                .filter(Authentication::isAuthenticated)
                .cast(UsernamePasswordAuthenticationToken.class)
                .map(auth -> (AuthenticatedUser) auth.getPrincipal())
                .flatMap(user -> aclService.hasPermission(user.userId(), PermissionTarget.controller(controller))
                        .filter(Boolean::booleanValue)
                        .flatMap(ignored -> aclService.hasPermission(user.userId(), PermissionTarget.endpoint(endpoint, action)))
                        .defaultIfEmpty(false))
                .map(AuthorizationDecision::new)
                .defaultIfEmpty(new AuthorizationDecision(false));
    }
}
