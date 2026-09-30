package com.ecommerce.userservice.controller;

import com.ecommerce.userservice.DTO.UserRequest;
import com.ecommerce.userservice.DTO.UserResponse;
import com.ecommerce.userservice.Service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "👤 User Management", description = "CRUD operations for users — Admin only for list/delete/status endpoints")
public class UserController {

    private final UserService userService;

    // ── Registration ──────────────────────────────────────────────────────────
    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Creates a new CUSTOMER (ACTIVE) or VENDOR (PENDING) account. VENDOR accounts require admin approval before login."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error — check email/mobile format"),
            @ApiResponse(responseCode = "409", description = "Email or mobile already registered")
    })
    public ResponseEntity<UserResponse> register(@RequestBody UserRequest request) {
        log.info("Request reach to register controller");
        UserResponse response = userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── Get All Users (Admin) ─────────────────────────────────────────────────
    @GetMapping
    @Operation(
            summary = "Get all users",
            description = "Returns all registered users. 🔒 Admin only."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of users returned"),
            @ApiResponse(responseCode = "403", description = "Forbidden — Admin role required")
    })
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // ── Get By ID ─────────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Returns user profile for the given UUID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "User UUID", required = true) @PathVariable("id") UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // ── Update User ───────────────────────────────────────────────────────────
    @PutMapping("/{id}")
    @Operation(summary = "Update user profile", description = "Updates firstName, lastName, mobile for a given user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "User UUID", required = true) @PathVariable UUID id,
            @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    // ── Update User Status (Admin) ────────────────────────────────────────────
    @PatchMapping("/{id}/status")
    @Operation(
            summary = "Update user status",
            description = "Changes a user's status to ACTIVE, PENDING, or BLOCKED. Use this to approve VENDORs. 🔒 Admin only."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated"),
            @ApiResponse(responseCode = "400", description = "Invalid status value — must be ACTIVE, PENDING, or BLOCKED"),
            @ApiResponse(responseCode = "403", description = "Forbidden — Admin role required"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserResponse> updateUserStatus(
            @Parameter(description = "User UUID", required = true) @PathVariable UUID id,
            @Parameter(description = "New status: ACTIVE | PENDING | BLOCKED", required = true)
            @RequestParam("status") String status) {
        return ResponseEntity.ok(userService.updateUserStatus(id, status));
    }

    // ── Delete User (Admin) ───────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user", description = "Permanently deletes a user account. 🔒 Admin only.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User deleted"),
            @ApiResponse(responseCode = "403", description = "Forbidden — Admin role required"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User UUID", required = true) @PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}