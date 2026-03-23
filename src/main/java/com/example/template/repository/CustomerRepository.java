package com.example.template.repository;

import com.example.template.repository.entity.CustomerEntity;
import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface CustomerRepository extends ReactiveCrudRepository<CustomerEntity, UUID> {
    Mono<Boolean> existsByEmail(String email);
}
