package com.ecommerce.userservice.Controller;


import com.TechPulse.LearnSpringSecurity.DTO.RegisterReq;
import com.TechPulse.LearnSpringSecurity.DTO.RegisterResp;
import com.TechPulse.LearnSpringSecurity.Exception.ApiResponse;
import com.TechPulse.LearnSpringSecurity.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/register")
public class RegisterController {

   private final UserService userService;


    @PostMapping
    public ResponseEntity<ApiResponse<RegisterResp>> regUser(@RequestBody RegisterReq registerReq){
        ApiResponse<RegisterResp> result = userService.createUser(registerReq);
            return ResponseEntity.ok(result);
    }
}
