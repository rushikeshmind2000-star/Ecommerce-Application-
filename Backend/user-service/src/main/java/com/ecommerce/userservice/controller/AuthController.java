package com.ecommerce.userservice.controller;

import com.ecommerce.userservice.DTO.AuthResponse;
import com.ecommerce.userservice.DTO.LoginRequest;
import com.ecommerce.userservice.Service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "🔐 Authentication", description = "Login endpoint — returns JWT access & refresh tokens. No auth required.")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(
            summary = "Login — get JWT tokens",
            description = "Validates email + password. Returns an **accessToken** (1 hour) and a **refreshToken** (24 hours).\n\n" +
                    "**Roles:**\n" +
                    "- `CUSTOMER` → immediate access\n" +
                    "- `VENDOR` → must be approved (status=ACTIVE) by Admin first\n" +
                    "- `ADMIN` → immediate access\n\n" +
                    "After login, copy the `accessToken` and click **Authorize** 🔒 at the top of Swagger."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful — JWT tokens returned"),
            @ApiResponse(responseCode = "401", description = "Invalid email or password"),
            @ApiResponse(responseCode = "403", description = "Account is PENDING approval or BLOCKED")
    })
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
