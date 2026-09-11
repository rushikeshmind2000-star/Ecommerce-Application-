package com.ecommerce.userservice.Controller;

import com.TechPulse.LearnSpringSecurity.DTO.CustomResponse;
import com.TechPulse.LearnSpringSecurity.Exception.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
@Slf4j
public class HealthController {

    @GetMapping
    public ResponseEntity<ApiResponse<CustomResponse>> healthCheck(){

        CustomResponse customResponse = new CustomResponse("health is checking.....");
        log.info(customResponse.getCustomMsg());


        ApiResponse<CustomResponse> response = ApiResponse.<CustomResponse>builder()
                .success(true)
                .message("Application is running successfully....")
                .data(customResponse).build();

        return ResponseEntity.ok(response);
    }
}
