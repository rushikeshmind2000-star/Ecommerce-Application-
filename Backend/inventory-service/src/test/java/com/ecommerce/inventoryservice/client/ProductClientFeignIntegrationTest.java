package com.ecommerce.inventoryservice.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.ecommerce.inventoryservice.client.dto.ProductResponse;
import com.ecommerce.inventoryservice.config.FeignConfig;
import com.ecommerce.inventoryservice.exceptions.ProductNotFoundException;
import com.ecommerce.inventoryservice.exceptions.ProductServiceUnavailableException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(
        classes = ProductClientFeignIntegrationTest.FeignTestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ProductClientFeignIntegrationTest {

    private static final AtomicInteger responseStatus = new AtomicInteger(200);
    private static final AtomicReference<String> responseBody =
            new AtomicReference<>();
    private static final AtomicReference<String> requestedMethod =
            new AtomicReference<>();
    private static final AtomicReference<String> requestedPath =
            new AtomicReference<>();
    private static final HttpServer productServer = startProductServer();

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class
    })
    @EnableFeignClients(clients = ProductClient.class)
    @Import(FeignConfig.class)
    static class FeignTestApplication {
    }

    @DynamicPropertySource
    static void configureFeignUrl(DynamicPropertyRegistry properties) {
        properties.add(
                "spring.cloud.openfeign.client.config.product-service.url",
                () -> "http://127.0.0.1:" + productServer.getAddress().getPort());
        properties.add("eureka.client.enabled", () -> "false");
        properties.add("spring.cloud.discovery.enabled", () -> "false");
    }

    @BeforeEach
    void resetServerResponse() {
        responseStatus.set(200);
        responseBody.set("""
                {
                  "id": "%s",
                  "name": "Integration Test Product",
                  "sku": "TEST-001",
                  "price": 12.50,
                  "currency": "INR",
                  "status": "ACTIVE",
                  "brand": "Test Brand"
                }
                """.formatted(TEST_PRODUCT_ID));
        requestedMethod.set(null);
        requestedPath.set(null);
    }

    private static final UUID TEST_PRODUCT_ID =
            UUID.fromString("a6a8adf3-e2cc-4c9f-8d64-c4aef92de263");

    @Autowired
    private ProductClient productClient;

    @Test
    void getProductById_SendsGetToExpectedPathAndDeserializesProductDto() {
        ProductResponse product = productClient.getProductById(TEST_PRODUCT_ID);

        assertEquals("GET", requestedMethod.get());
        assertEquals(
                "/api/products/" + TEST_PRODUCT_ID,
                requestedPath.get());
        assertEquals(TEST_PRODUCT_ID, product.getId());
        assertEquals("Integration Test Product", product.getName());
        assertEquals("TEST-001", product.getSku());
        assertEquals("12.50", product.getPrice().toPlainString());
        assertEquals("INR", product.getCurrency());
        assertEquals("ACTIVE", product.getStatus());
    }

    @Test
    void getProductById_MapsProductNotFoundResponse() {
        responseStatus.set(404);
        responseBody.set("{\"message\":\"Product not found\"}");

        assertThrows(
                ProductNotFoundException.class,
                () -> productClient.getProductById(TEST_PRODUCT_ID));
    }

    @Test
    void getProductById_MapsProductServerError() {
        responseStatus.set(503);
        responseBody.set("{\"message\":\"Temporarily unavailable\"}");

        assertThrows(
                ProductServiceUnavailableException.class,
                () -> productClient.getProductById(TEST_PRODUCT_ID));
    }

    @AfterAll
    static void stopProductServer() {
        productServer.stop(0);
    }

    private static HttpServer startProductServer() {
        try {
            HttpServer server = HttpServer.create(
                    new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/api/products/", ProductClientFeignIntegrationTest::respond);
            server.start();
            return server;
        } catch (IOException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private static void respond(HttpExchange exchange) throws IOException {
        requestedMethod.set(exchange.getRequestMethod());
        requestedPath.set(exchange.getRequestURI().getPath());
        byte[] body = responseBody.get().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(responseStatus.get(), body.length);
        try (var output = exchange.getResponseBody()) {
            output.write(body);
        }
    }
}
