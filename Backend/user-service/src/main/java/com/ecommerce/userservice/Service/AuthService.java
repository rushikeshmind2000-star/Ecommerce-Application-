package com.ecommerce.userservice.Service;

import com.ecommerce.userservice.DTO.AuthResponse;
import com.ecommerce.userservice.DTO.LoginRequest;
import com.ecommerce.userservice.Entity.UserEntity;
import com.ecommerce.userservice.Enums.Status;
import com.ecommerce.userservice.Repo.UserRepository;
import com.ecommerce.userservice.Security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse login(LoginRequest request) {

        // 1. Find user by email
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // 2. Validate password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        // 3. Check vendor/user is approved (not PENDING or BLOCKED)
        if (user.getStatus() == Status.PENDING) {
            throw new RuntimeException("Your account is pending admin approval. Please wait.");
        }
        if (user.getStatus() == Status.BLOCKED) {
            throw new RuntimeException("Your account has been blocked. Contact support.");
        }

        // 4. Generate tokens
        String role = user.getRole().name();
        String accessToken  = jwtUtil.generateAccessToken(user.getId(), user.getEmail(), role);
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail(), role);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .role(role)
                .userId(user.getId())
                .email(user.getEmail())
                .build();
    }
}
