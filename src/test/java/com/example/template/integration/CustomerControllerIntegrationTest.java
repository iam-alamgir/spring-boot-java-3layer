package com.example.template.integration;

import com.example.template.TemplateApplication;
import com.example.template.repository.AclPermissionRepository;
import com.example.template.repository.UserRepository;
import com.example.template.repository.entity.AclPermissionEntity;
import com.example.template.repository.entity.UserEntity;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@SpringBootTest(classes = TemplateApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class CustomerControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AclPermissionRepository aclPermissionRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        var userId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        userRepository.deleteAll()
                .then(aclPermissionRepository.deleteAll())
                .then(userRepository.save(new UserEntity(userId, "integration-user", passwordEncoder.encode("secret"),
                        "Integration User", true, OffsetDateTime.now(), OffsetDateTime.now())))
                .thenMany(aclPermissionRepository.saveAll(java.util.List.of(
                        new AclPermissionEntity(UUID.randomUUID(), userId, "CONTROLLER", "CustomerController", "ACCESS", true, OffsetDateTime.now()),
                        new AclPermissionEntity(UUID.randomUUID(), userId, "ENDPOINT", "CUSTOMER_API", "CREATE", true, OffsetDateTime.now()),
                        new AclPermissionEntity(UUID.randomUUID(), userId, "ENDPOINT", "CUSTOMER_API", "READ", true, OffsetDateTime.now())
                )))
                .then().block();
    }

    @Test
    void shouldCreateCustomerEndToEnd() {
        var tokenResponse = webTestClient.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"username":"integration-user","password":"secret"}
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").value(String.class::cast)
                .returnResult();

        var accessToken = tokenResponse.getResponseBodyContent() == null ? "" : new String(tokenResponse.getResponseBodyContent());
        var token = accessToken.replaceAll(".*\"accessToken\":\"([^\"]+)\".*", "$1");

        webTestClient.post()
                .uri("/api/v1/customers")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"fullName":"Jane Integration","email":"jane.integration@example.com"}
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.email").isEqualTo("jane.integration@example.com");
    }
}
