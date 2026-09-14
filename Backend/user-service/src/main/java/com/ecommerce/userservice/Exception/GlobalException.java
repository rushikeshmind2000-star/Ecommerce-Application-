package com.ecommerce.userservice.Exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalException {


    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String ,Object>> AccessDeniedHandler(AccessDeniedException ex){
        return  buildResponse(HttpStatus.FORBIDDEN,ex.getMessage());
    }



    public ResponseEntity<Map<String,Object>> UserAlreadyExists(UserAlreadyExists ex){
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }


    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
