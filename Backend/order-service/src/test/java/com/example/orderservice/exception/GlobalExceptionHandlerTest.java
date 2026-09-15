package com.example.orderservice.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pure unit tests for {@link GlobalExceptionHandler}.
 *
 * Each handler is invoked directly (no Spring context) and the response
 * entity's status code and body are asserted.
 */
@DisplayName("GlobalExceptionHandler Unit Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    // ------------------------------------------------------------------
    // 404 — OrderNotFoundException
    // ------------------------------------------------------------------

    @Test
    @DisplayName("should return 404 with structured body for OrderNotFoundException")
    void handleOrderNotFound_returns404() {
        OrderNotFoundException ex =
                new OrderNotFoundException("Order not found with id: abc-123");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                handler.handleOrderNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().error()).isEqualTo("Order Not Found");
        assertThat(response.getBody().message()).contains("abc-123");
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    // ------------------------------------------------------------------
    // 409 — OrderCancellationException
    // ------------------------------------------------------------------

    @Test
    @DisplayName("should return 409 with structured body for OrderCancellationException")
    void handleOrderCancellation_returns409() {
        OrderCancellationException ex =
                new OrderCancellationException("Order 'ORD-X' cannot be cancelled; already DELIVERED.");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                handler.handleOrderCancellation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(409);
        assertThat(response.getBody().error()).isEqualTo("Order Cannot Be Cancelled");
        assertThat(response.getBody().message()).contains("DELIVERED");
    }

    // ------------------------------------------------------------------
    // 400 — MethodArgumentNotValidException (bean validation)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("should return 400 with fieldErrors map for validation failure")
    void handleValidation_returns400WithFieldErrors() throws Exception {
        // Build a synthetic BindingResult with one field error
        Object target = new Object();
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(target, "createOrderRequest");
        bindingResult.addError(
                new FieldError("createOrderRequest", "userId", "User ID is required"));

        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<GlobalExceptionHandler.ValidationErrorResponse> response =
                handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Validation Failed");

        Map<String, String> fieldErrors = response.getBody().fieldErrors();
        assertThat(fieldErrors).containsEntry("userId", "User ID is required");
    }

    @Test
    @DisplayName("should capture all field errors for multi-field validation failure")
    void handleValidation_multipleErrors() throws Exception {
        Object target = new Object();
        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(target, "createOrderRequest");
        bindingResult.addError(new FieldError("req", "userId", "User ID is required"));
        bindingResult.addError(new FieldError("req", "items", "Order must contain at least one item"));

        MethodArgumentNotValidException ex =
                new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<GlobalExceptionHandler.ValidationErrorResponse> response =
                handler.handleValidation(ex);

        assertThat(response.getBody().fieldErrors()).hasSize(2);
        assertThat(response.getBody().fieldErrors()).containsKey("userId");
        assertThat(response.getBody().fieldErrors()).containsKey("items");
    }

    // ------------------------------------------------------------------
    // 400 — HttpMessageNotReadableException (malformed JSON / bad enum)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("should return 400 for malformed/unreadable request body")
    void handleNotReadable_returns400() {
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
                "JSON parse error",
                new MockHttpInputMessage("{bad json}".getBytes(StandardCharsets.UTF_8))
        );

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                handler.handleNotReadable(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().error()).isEqualTo("Malformed Request");
        assertThat(response.getBody().message())
                .contains("missing", "malformed", "invalid");
    }

    // ------------------------------------------------------------------
    // 405 — HttpRequestMethodNotSupportedException
    // ------------------------------------------------------------------

    @Test
    @DisplayName("should return 405 with method name in message")
    void handleMethodNotSupported_returns405() {
        HttpRequestMethodNotSupportedException ex =
                new HttpRequestMethodNotSupportedException("PUT");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                handler.handleMethodNotSupported(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(405);
        assertThat(response.getBody().error()).isEqualTo("Method Not Allowed");
        assertThat(response.getBody().message()).contains("PUT");
    }

    // ------------------------------------------------------------------
    // 500 — Generic Exception
    // ------------------------------------------------------------------

    @Test
    @DisplayName("should return 500 for unexpected exceptions, hiding internal details")
    void handleGeneric_returns500() {
        RuntimeException ex = new RuntimeException("NullPointerException in some service");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                handler.handleGeneric(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(500);
        assertThat(response.getBody().error()).isEqualTo("Internal Server Error");
        // Must NOT expose internal exception message to the client
        assertThat(response.getBody().message())
                .doesNotContain("NullPointerException");
    }
}
