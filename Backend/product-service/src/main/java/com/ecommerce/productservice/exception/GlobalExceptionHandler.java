package com.ecommerce.productservice.exception;

import com.ecommerce.productservice.enums.ApiKey;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<ApiKey, Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        Map<ApiKey, Object> response = new EnumMap<>(ApiKey.class);
        response.put(ApiKey.success, false);
        response.put(ApiKey.message, ex.getMessage());
        response.put(ApiKey.data, Collections.emptyMap());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResourceCreationException.class)
    public ResponseEntity<Map<ApiKey, Object>> handleResourceCreationException(ResourceCreationException ex) {
        Map<ApiKey, Object> response = new EnumMap<>(ApiKey.class);
        response.put(ApiKey.success, false);
        response.put(ApiKey.message, ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<ApiKey, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<ApiKey, Object> response = new EnumMap<>(ApiKey.class);
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage()));
        response.put(ApiKey.success, false);
        response.put(ApiKey.message, "Validation failed");
        response.put(ApiKey.data, errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<ApiKey, Object>> handleGlobalException(Exception ex) {
        Map<ApiKey, Object> response = new EnumMap<>(ApiKey.class);
        response.put(ApiKey.success, false);
        response.put(ApiKey.message, ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
