package com.ecommerce.wishlistservice.exception;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the Wishlist Service.
 * Returns consistent, structured JSON error responses.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── 404 Not Found ──────────────────────────────────────────────────────────
    @ExceptionHandler(WishlistItemNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(WishlistItemNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage());
    }

    // ── 409 Conflict (Duplicate) ────────────────────────────────────────────────
    @ExceptionHandler(DuplicateWishlistItemException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicate(DuplicateWishlistItemException ex) {
        return buildResponse(HttpStatus.CONFLICT, "Conflict", ex.getMessage());
    }

    // ── 400 Validation errors ───────────────────────────────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }
        Map<String, Object> body = buildBody(HttpStatus.BAD_REQUEST, "Validation Failed", "One or more fields are invalid");
        body.put("fieldErrors", fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    // ── 404 Feign: downstream service returned 404 ─────────────────────────────
    @ExceptionHandler(FeignException.NotFound.class)
    public ResponseEntity<Map<String, Object>> handleFeignNotFound(FeignException.NotFound ex) {
        log.warn("Downstream service returned 404: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "Not Found",
                "Referenced resource does not exist in the upstream service.");
    }

    // ── 503 Feign: downstream service is unavailable ───────────────────────────
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Map<String, Object>> handleFeign(FeignException ex) {
        log.error("Feign client error (status={}): {}", ex.status(), ex.getMessage());
        if (ex.status() == 404) {
            return buildResponse(HttpStatus.NOT_FOUND, "Not Found",
                    "Referenced resource does not exist in the upstream service.");
        }
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable",
                "A required upstream service is temporarily unavailable. Please try again.");
    }

    // ── 500 Catch-all ──────────────────────────────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred. Please contact support.");
    }

    // ── Helpers ────────────────────────────────────────────────────────────────
    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String error, String message) {
        return ResponseEntity.status(status).body(buildBody(status, error, message));
    }

    private Map<String, Object> buildBody(HttpStatus status, String error, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        body.put("timestamp", Instant.now().toString());
        return body;
    }
}
