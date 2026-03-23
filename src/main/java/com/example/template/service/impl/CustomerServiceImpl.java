package com.example.template.service.impl;

import com.example.template.dto.CustomerRequest;
import com.example.template.dto.CustomerResponse;
import com.example.template.exception.ConflictException;
import com.example.template.mapper.CustomerMapper;
import com.example.template.repository.CustomerRepository;
import com.example.template.repository.entity.CustomerEntity;
import com.example.template.service.CustomerService;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.time.OffsetDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class CustomerServiceImpl implements CustomerService {

    private static final Logger BUSINESS_LOG = LoggerFactory.getLogger("BUSINESS_LOG");

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;
    private final RateLimiterRegistry rateLimiterRegistry;

    public CustomerServiceImpl(CustomerRepository customerRepository, CustomerMapper customerMapper,
                               CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry,
                               RateLimiterRegistry rateLimiterRegistry) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.retryRegistry = retryRegistry;
        this.rateLimiterRegistry = rateLimiterRegistry;
    }

    @Override
    public Mono<CustomerResponse> create(CustomerRequest request) {
        return customerRepository.existsByEmail(request.email())
                .flatMap(exists -> exists
                        ? Mono.<CustomerEntity>error(new ConflictException("Customer email already exists"))
                        : customerRepository.save(new CustomerEntity(
                                UUID.randomUUID(),
                                "CUST-" + UUID.randomUUID().toString().substring(0, 8),
                                request.fullName(),
                                request.email(),
                                OffsetDateTime.now(ZoneOffset.UTC),
                                OffsetDateTime.now(ZoneOffset.UTC)
                        )))
                .map(customerMapper::toResponse)
                .doOnNext(response -> BUSINESS_LOG.info("customer-created externalRef={} email={}", response.externalRef(), response.email()))
                .transformDeferred(CircuitBreakerOperator.of(circuitBreakerRegistry.circuitBreaker("customerCreate")))
                .transformDeferred(RetryOperator.of(retryRegistry.retry("customerCreate")))
                .transformDeferred(RateLimiterOperator.of(rateLimiterRegistry.rateLimiter("customerCreate")));
    }

    @Override
    public Flux<CustomerResponse> findAll() {
        return customerRepository.findAll().map(customerMapper::toResponse);
    }
}
