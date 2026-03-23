package com.example.template.security;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class AclMetadataWebFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        var path = exchange.getRequest().getPath().value();
        if (path.startsWith("/api/v1/customers")) {
            exchange.getAttributes().put("acl.controller", "CustomerController");
            exchange.getAttributes().put("acl.endpoint", "CUSTOMER_API");
            exchange.getAttributes().put("acl.action", switch (exchange.getRequest().getMethod()) {
                case GET -> "READ";
                case POST -> "CREATE";
                default -> exchange.getRequest().getMethod().name();
            });
        }
        return chain.filter(exchange);
    }
}
