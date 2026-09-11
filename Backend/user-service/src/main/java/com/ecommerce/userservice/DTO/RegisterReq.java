package com.ecommerce.userservice.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterReq {

    @Email
    private  String email;

    @NotBlank
    private String password;

    @NotEmpty
    private  String role;;
}
