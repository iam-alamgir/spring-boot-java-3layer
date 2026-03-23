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
        } else if (path.startsWith("/api/v1/admin/tokens/users/")) {
            exchange.getAttributes().put("acl.controller", "AdminController");
            exchange.getAttributes().put("acl.endpoint", "TOKEN_ADMIN_API");
            exchange.getAttributes().put("acl.action", "CLEAR_USER");
        } else if (path.equals("/api/v1/admin/tokens")) {
            exchange.getAttributes().put("acl.controller", "AdminController");
            exchange.getAttributes().put("acl.endpoint", "TOKEN_ADMIN_API");
            exchange.getAttributes().put("acl.action", "CLEAR_ALL");
        } else if (path.startsWith("/api/v1/admin/acl/users/")) {
            exchange.getAttributes().put("acl.controller", "AdminController");
            exchange.getAttributes().put("acl.endpoint", "ACL_ADMIN_API");
            exchange.getAttributes().put("acl.action", "REFRESH_USER");
        } else if (path.equals("/api/v1/admin/acl/refresh")) {
            exchange.getAttributes().put("acl.controller", "AdminController");
            exchange.getAttributes().put("acl.endpoint", "ACL_ADMIN_API");
            exchange.getAttributes().put("acl.action", "REFRESH_ALL");
        } else if (path.startsWith("/api/v1/uploads/records")) {
            exchange.getAttributes().put("acl.controller", "UploadController");
            exchange.getAttributes().put("acl.endpoint", "UPLOAD_API");
            exchange.getAttributes().put("acl.action", "PROCESS");
        }
        return chain.filter(exchange);
    }
}
