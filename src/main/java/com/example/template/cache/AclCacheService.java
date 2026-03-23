package com.example.template.cache;

import com.example.template.config.ApplicationProperties;
import java.util.List;
import java.util.UUID;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AclCacheService {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ApplicationProperties properties;

    public AclCacheService(ReactiveStringRedisTemplate redisTemplate, ApplicationProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    public Mono<List<String>> getPermissions(UUID userId) {
        return redisTemplate.opsForSet().members(CacheKeys.aclPermissions(userId)).collectList();
    }

    public Mono<Void> cachePermissions(UUID userId, List<String> permissions) {
        return redisTemplate.delete(CacheKeys.aclPermissions(userId))
                .thenMany(permissions.isEmpty()
                        ? Flux.empty()
                        : redisTemplate.opsForSet().add(CacheKeys.aclPermissions(userId), permissions.toArray(String[]::new)).flux())
                .then(redisTemplate.expire(CacheKeys.aclPermissions(userId), properties.cache().aclTtl()))
                .then();
    }

    public Mono<Void> evictUser(UUID userId) {
        return redisTemplate.delete(CacheKeys.aclPermissions(userId)).then();
    }

    public Mono<Void> evictAll() {
        return redisTemplate.scan().filter(key -> key.startsWith("acl:user:"))
                .collectList()
                .flatMap(keys -> keys.isEmpty() ? Mono.empty() : redisTemplate.delete(Flux.fromIterable(keys)).then())
                .then();
    }
}
