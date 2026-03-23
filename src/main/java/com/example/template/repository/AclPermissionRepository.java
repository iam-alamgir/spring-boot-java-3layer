package com.example.template.repository;

import com.example.template.repository.entity.AclPermissionEntity;
import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface AclPermissionRepository extends ReactiveCrudRepository<AclPermissionEntity, UUID> {
    Flux<AclPermissionEntity> findByUserIdAndGrantedTrue(UUID userId);
}
