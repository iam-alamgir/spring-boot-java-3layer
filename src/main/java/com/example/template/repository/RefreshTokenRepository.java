package com.example.template.repository;

import com.example.template.repository.entity.RefreshTokenEntity;
import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface RefreshTokenRepository extends ReactiveCrudRepository<RefreshTokenEntity, UUID> {
    Mono<RefreshTokenEntity> findByUserIdAndRevokedFalse(UUID userId);
}
