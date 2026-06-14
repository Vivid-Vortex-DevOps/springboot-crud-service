package com.crud.product;

import com.crud.product.dto.ProductRequest;
import com.crud.product.dto.ProductResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("dev")
class ProductIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("springboot_service_db")
        .withUsername("cruduser")
        .withPassword("testpassword");

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void crudLifecycle() {
        // CREATE
        var req = new ProductRequest("Widget", "A test widget", new BigDecimal("9.99"), 10);
        ResponseEntity<ProductResponse> createResp = restTemplate.postForEntity(
            "/api/v1/products", req, ProductResponse.class);

        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResp.getHeaders().getLocation()).isNotNull();
        Long id = createResp.getBody().id();

        // READ
        ResponseEntity<ProductResponse> getResp = restTemplate.getForEntity(
            "/api/v1/products/" + id, ProductResponse.class);
        assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResp.getBody().name()).isEqualTo("Widget");

        // UPDATE
        var updateReq = new ProductRequest("Widget v2", "Updated", new BigDecimal("19.99"), 5);
        restTemplate.put("/api/v1/products/" + id, updateReq);
        ResponseEntity<ProductResponse> updated = restTemplate.getForEntity(
            "/api/v1/products/" + id, ProductResponse.class);
        assertThat(updated.getBody().name()).isEqualTo("Widget v2");

        // DELETE
        restTemplate.delete("/api/v1/products/" + id);
        ResponseEntity<String> deletedGet = restTemplate.getForEntity(
            "/api/v1/products/" + id, String.class);
        assertThat(deletedGet.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void create_returns400_whenNameBlank() {
        var req = new ProductRequest("", null, new BigDecimal("1.00"), 0);
        ResponseEntity<String> resp = restTemplate.postForEntity(
            "/api/v1/products", req, String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void healthEndpoints_return200() {
        assertThat(restTemplate.getForEntity("/actuator/health/liveness", String.class)
            .getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(restTemplate.getForEntity("/actuator/health/readiness", String.class)
            .getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
