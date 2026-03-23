package com.example.template.controller;

import com.example.template.dto.CustomerRequest;
import com.example.template.dto.CustomerResponse;
import com.example.template.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Tag(name = "Customers", description = "Sample controller demonstrating ACL-protected reactive endpoints")
@SecurityRequirement(name = "bearer-jwt")
@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Operation(summary = "Create a customer", description = "Creates a new customer when ACL grants CREATE access to the endpoint.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Customer created"),
            @ApiResponse(responseCode = "403", description = "ACL denied", content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "Duplicate email", content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) {
        return customerService.create(request);
    }

    @Operation(summary = "List customers", description = "Returns all customers when ACL grants READ access to the endpoint.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Customers returned",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CustomerResponse.class)))),
            @ApiResponse(responseCode = "403", description = "ACL denied", content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping
    public Flux<CustomerResponse> findAll() {
        return customerService.findAll();
    }
}
