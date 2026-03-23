package com.example.template.service;

import com.example.template.dto.CustomerRequest;
import com.example.template.dto.CustomerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CustomerService {
    Mono<CustomerResponse> create(CustomerRequest request);

    Flux<CustomerResponse> findAll();
}
