package com.ecommerce.userservice.Service;
import com.ecommerce.userservice.DTO.UserRequest;
import com.ecommerce.userservice.DTO.UserResponse;
import com.ecommerce.userservice.Entity.UserEntity;
import com.ecommerce.userservice.Repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final KeycloakService keycloakService;

    public UserResponse registerUser(UserRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        if (userRepository.existsByMobile(request.getMobile())) {
            throw new RuntimeException("Mobile already registered");
        }

        // Create user in Keycloak
        String keycloakUserId =
                keycloakService.createUser(
                        request.getEmail(),
                        request.getPassword(),
                        request.getFirstName(),
                        request.getLastName()
                );

        // Save application-specific information
        UserEntity user = UserEntity.builder()
                .keycloakUserId(keycloakUserId)
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .mobile(request.getMobile())
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

    public UserResponse updateUser(
            UUID id,
            UserRequest request) {

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
                .keycloakUserId(user.getKeycloakUserId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .mobile(user.getMobile())
                .build();
    }
}