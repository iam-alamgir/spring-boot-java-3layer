package com.example.template.admin;

import com.example.template.acl.AclService;
import com.example.template.cache.TokenCacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Tag(name = "Administration", description = "Administrative cache and token maintenance endpoints")
@SecurityRequirement(name = "bearer-jwt")
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final TokenCacheService tokenCacheService;
    private final AclService aclService;

    public AdminController(TokenCacheService tokenCacheService, AclService aclService) {
        this.tokenCacheService = tokenCacheService;
        this.aclService = aclService;
    }

    @Operation(summary = "Clear all cached tokens for a user")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "User tokens cleared")})
    @DeleteMapping("/tokens/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> clearUserTokens(@PathVariable UUID userId) {
        return tokenCacheService.clearTokensByUser(userId);
    }

    @Operation(summary = "Clear all cached access and refresh tokens")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "All tokens cleared")})
    @DeleteMapping("/tokens")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> clearAllTokens() {
        return tokenCacheService.clearAllTokens();
    }

    @Operation(summary = "Refresh ACL cache for a user")
    @PostMapping("/acl/users/{userId}/refresh")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<Void> refreshUserAcl(@PathVariable UUID userId) {
        return aclService.refreshUserPermissions(userId);
    }

    @Operation(summary = "Refresh ACL cache for all users")
    @PostMapping("/acl/refresh")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<Void> refreshAllAcl() {
        return aclService.refreshAllPermissions();
    }
}
