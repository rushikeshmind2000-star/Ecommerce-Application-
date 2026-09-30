package com.ecommerce.userservice.DTO;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private UUID id;

    private String firstName;

    private String lastName;

    private String email;

    private String mobile;

    private String password; // BCrypt hashed — for internal use only, do NOT expose in production

    private String role;

    private String status;
}
