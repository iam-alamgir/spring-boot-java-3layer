package com.example.template.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.template.dto.CustomerRequest;
import com.example.template.mapper.CustomerMapper;
import com.example.template.repository.CustomerRepository;
import com.example.template.repository.entity.CustomerEntity;
import com.example.template.service.impl.CustomerServiceImpl;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    private CustomerServiceImpl customerService;

    @BeforeEach
    void setUp() {
        CustomerMapper customerMapper = Mappers.getMapper(CustomerMapper.class);
        customerService = new CustomerServiceImpl(customerRepository, customerMapper,
                CircuitBreakerRegistry.ofDefaults(), RetryRegistry.ofDefaults(), RateLimiterRegistry.ofDefaults());
    }

    @Test
    void createShouldPersistCustomer() {
        var request = new CustomerRequest("Jane Roe", "jane@example.com");
        var entity = new CustomerEntity(UUID.randomUUID(), "CUST-0001", "Jane Roe", "jane@example.com",
                OffsetDateTime.now(), OffsetDateTime.now());

        when(customerRepository.existsByEmail(request.email())).thenReturn(Mono.just(false));
        when(customerRepository.save(any(CustomerEntity.class))).thenReturn(Mono.just(entity));

        StepVerifier.create(customerService.create(request))
                .expectNextMatches(response -> response.email().equals("jane@example.com")
                        && response.externalRef().equals("CUST-0001"))
                .verifyComplete();
    }
}
