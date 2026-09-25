package com.ecommerce.userservice.Service;

import com.ecommerce.userservice.DTO.UserRequest;
import com.ecommerce.userservice.DTO.UserResponse;
import com.ecommerce.userservice.Entity.UserEntity;
import com.ecommerce.userservice.Enums.Role;
import com.ecommerce.userservice.Enums.Status;
import com.ecommerce.userservice.Repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserResponse registerUser(UserRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        if (userRepository.existsByMobile(request.getMobile())) {
            throw new RuntimeException("Mobile already registered");
        }

        // Determine Role and Status
        Role userRole = Role.CUSTOMER;
        Status userStatus = Status.ACTIVE;

        if (request.getRole() != null) {
            try {
                userRole = Role.valueOf(request.getRole().toUpperCase());
                if (userRole == Role.VENDOR) {
                    userStatus = Status.PENDING; // Vendors require admin approval
                } else if (userRole == Role.ADMIN) {
                    // Usually admin accounts are seeded, but for now we'll allow it and make it ACTIVE
                    userStatus = Status.ACTIVE;
                }
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid role provided");
            }
        }

        // Save user directly to DB
        UserEntity user = UserEntity.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .mobile(request.getMobile())
                .role(userRole)
                .status(userStatus)
                .build();

        UserEntity savedUser = userRepository.save(user);

        return mapToResponse(savedUser);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public UserResponse getUserById(UUID id) {

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return mapToResponse(user);
    }

    public UserResponse updateUser(UUID id, UserRequest request) {

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setMobile(request.getMobile());

        return mapToResponse(userRepository.save(user));
    }

    public void deleteUser(UUID id) {

        UserEntity user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        userRepository.delete(user);
    }

    private UserResponse mapToResponse(UserEntity user) {

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .mobile(user.getMobile())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .build();
    }
}