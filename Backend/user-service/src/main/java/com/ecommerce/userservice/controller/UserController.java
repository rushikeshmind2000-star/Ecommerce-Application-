package com.ecommerce.userservice.controller;

import com.ecommerce.userservice.DTO.UserRequest;
import com.ecommerce.userservice.DTO.UserResponse;
import com.ecommerce.userservice.Service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {


    private final UserService userService;

    // Registration
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @RequestBody UserRequest request) {
log.info("Request reach to register controller ");
        UserResponse response = userService.registerUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Get all users
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {

        return ResponseEntity.ok(userService.getAllUsers());
    }

    // Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable("id") UUID id) {

        return ResponseEntity.ok(userService.getUserById(id));
    }

    // Update user
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID id,
            @RequestBody UserRequest request) {

        return ResponseEntity.ok(
                userService.updateUser(id, request)
        );
    }

    // Delete user
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable UUID id) {

        userService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }
}