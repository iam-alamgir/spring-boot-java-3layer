package com.example.template.cache;

import com.example.template.config.ApplicationProperties;
import java.util.UUID;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class TokenCacheService {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final ApplicationProperties properties;

    public TokenCacheService(ReactiveStringRedisTemplate redisTemplate, ApplicationProperties properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    public Mono<Void> cacheAccessToken(UUID userId, String token) {
        var tokenHash = HashingSupport.sha256(token);
        return redisTemplate.opsForValue().set(CacheKeys.accessToken(tokenHash), userId.toString(), properties.jwt().accessTokenTtl())
                .then(redisTemplate.opsForSet().add(CacheKeys.userTokens(userId), CacheKeys.accessToken(tokenHash)))
                .then();
    }

    public Mono<Void> cacheRefreshToken(UUID userId, String token) {
        var tokenHash = HashingSupport.sha256(token);
        return redisTemplate.opsForValue().set(CacheKeys.refreshToken(tokenHash), userId.toString(), properties.jwt().refreshTokenTtl())
                .then(redisTemplate.opsForSet().add(CacheKeys.userTokens(userId), CacheKeys.refreshToken(tokenHash)))
                .then();
    }

    public Mono<Boolean> isAccessTokenActive(String token) {
        return redisTemplate.hasKey(CacheKeys.accessToken(HashingSupport.sha256(token)));
    }

    public Mono<Boolean> isRefreshTokenActive(String token) {
        return redisTemplate.hasKey(CacheKeys.refreshToken(HashingSupport.sha256(token)));
    }

    public Mono<Void> revokeRefreshToken(UUID userId, String token) {
        var key = CacheKeys.refreshToken(HashingSupport.sha256(token));
        return redisTemplate.delete(key)
                .then(redisTemplate.opsForSet().remove(CacheKeys.userTokens(userId), key))
                .then();
    }

    public Mono<Void> clearTokensByUser(UUID userId) {
        return redisTemplate.opsForSet().members(CacheKeys.userTokens(userId))
                .collectList()
                .flatMap(keys -> keys.isEmpty() ? Mono.empty() : redisTemplate.delete(Flux.fromIterable(keys)).then())
                .then(redisTemplate.delete(CacheKeys.userTokens(userId)).then())
                .then();
    }

    public Mono<Void> clearAllTokens() {
        return redisTemplate.scan().filter(key -> key.startsWith("token:"))
                .collectList()
                .flatMap(keys -> keys.isEmpty() ? Mono.empty() : redisTemplate.delete(Flux.fromIterable(keys)).then())
                .then();
    }
}
