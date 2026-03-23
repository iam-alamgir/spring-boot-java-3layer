package com.example.template.acl;

import com.example.template.cache.AclCacheService;
import com.example.template.repository.AclPermissionRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AclService {

    private final AclPermissionRepository aclPermissionRepository;
    private final AclCacheService aclCacheService;

    public AclService(AclPermissionRepository aclPermissionRepository, AclCacheService aclCacheService) {
        this.aclPermissionRepository = aclPermissionRepository;
        this.aclCacheService = aclCacheService;
    }

    public Mono<Boolean> hasPermission(UUID userId, PermissionTarget target) {
        return aclCacheService.getPermissions(userId)
                .flatMap(cached -> cached.isEmpty()
                        ? loadAndCachePermissions(userId)
                        : Mono.just(cached))
                .map(permissions -> permissions.contains(permissionKey(target)));
    }

    public Mono<Void> refreshUserPermissions(UUID userId) {
        return loadAndCachePermissions(userId).then();
    }

    public Mono<Void> refreshAllPermissions() {
        return aclPermissionRepository.findAll()
                .groupBy(permission -> permission.userId())
                .flatMap(group -> group.map(permission -> permission.resourceType() + ":" + permission.resourceKey() + ":" + permission.action())
                        .collectList()
                        .flatMap(permissions -> aclCacheService.cachePermissions(group.key(), permissions)))
                .then();
    }

    private Mono<List<String>> loadAndCachePermissions(UUID userId) {
        return aclPermissionRepository.findByUserIdAndGrantedTrue(userId)
                .map(permission -> permission.resourceType() + ":" + permission.resourceKey() + ":" + permission.action())
                .collectList()
                .flatMap(permissions -> aclCacheService.cachePermissions(userId, permissions).thenReturn(permissions));
    }

    private String permissionKey(PermissionTarget target) {
        return target.resourceType() + ":" + target.resourceKey() + ":" + target.action();
    }
}
