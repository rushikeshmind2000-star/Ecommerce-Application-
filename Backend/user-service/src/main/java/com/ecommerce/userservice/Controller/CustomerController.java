package com.ecommerce.userservice.Controller;


import com.TechPulse.LearnSpringSecurity.DTO.CustomResponse;
import com.TechPulse.LearnSpringSecurity.Exception.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customer")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerController {

    @GetMapping
    public ResponseEntity<ApiResponse<CustomResponse>> customerCheck(Authentication authentication){

        CustomResponse customResponse= new CustomResponse("Logged in as: " + authentication.getName()
                + ", Roles: " + authentication.getAuthorities());

        ApiResponse<CustomResponse> response = ApiResponse.<CustomResponse>builder()
                .success(true)
                .message("Customer is running....")
                .data(customResponse).build();
        return ResponseEntity.ok(response);
    }
}
