package com.ecommerce.userservice.Service;


import com.TechPulse.LearnSpringSecurity.DTO.RegisterReq;
import com.TechPulse.LearnSpringSecurity.DTO.RegisterResp;
import com.TechPulse.LearnSpringSecurity.Entity.UserEntity;
import com.TechPulse.LearnSpringSecurity.Enums.Role;
import com.TechPulse.LearnSpringSecurity.Exception.ApiResponse;
import com.TechPulse.LearnSpringSecurity.Repo.UserRepo;
import jakarta.transaction.Transactional;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@Builder
public class UserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;


    @Transactional
    public ApiResponse<RegisterResp> createUser(RegisterReq registerReq) {

        if (userRepo.existsByEmail(registerReq.getEmail())) {
            log.warn("User creation failed. Email already exists: {}", registerReq.getEmail());

            RegisterResp registerResp = RegisterResp.builder().user(registerReq.getEmail()).role(String.valueOf(registerReq.getRole())).build();


            return ApiResponse.<RegisterResp>builder()
                    .success(false)
                    .message("User Already Exists....")
                    .data(registerResp)
                    .build();
        }

        UserEntity userEntity = UserEntity.builder()
                .email(registerReq.getEmail())
                .password(passwordEncoder.encode(registerReq.getPassword()))
                .role(Role.valueOf(registerReq.getRole())).build();

        UserEntity result = userRepo.save(userEntity);
        RegisterResp registerResp = RegisterResp.builder().user(result.getEmail()).role(String.valueOf(result.getRole())).build();


        return ApiResponse.<RegisterResp>builder()
                .success(true)
                .message("User Register Successfully....")
                .data(registerResp)
                .build();


    }

}

